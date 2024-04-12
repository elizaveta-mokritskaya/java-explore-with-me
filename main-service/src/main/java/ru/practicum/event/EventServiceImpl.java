package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.ConfirmedRequest;
import ru.practicum.request.Request;
import ru.practicum.request.RequestRepository;
import ru.practicum.request.RequestStatus;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.statdto.HitOutcomeDto;
import ru.practicum.statsclient.StatisticsClient;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.request.RequestStatus.CONFIRMED;
import static ru.practicum.request.RequestStatus.REJECTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository repository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final StatisticsClient statisticsClient;

    @Override
    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        dateValidate(rangeStart, rangeEnd);
        rangeStart = rangeStart == null ? LocalDateTime.now() : rangeStart;
        List<Event> events = repository.findAllForAdmin(users, states, categories, rangeStart,
                PageRequest.of(from, size, Sort.unsorted()));
        setConfirmedRequests(events);
        return events.stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventByIdAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event event = repository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException("По заданному id нет события"));
        updateEventAdmin(event, dto);
        event = repository.save(event);
        locationRepository.save(event.getLocation());
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getAllEventsByUserId(Long userId, int from, int size) {
        if (!userRepository.existsUserById(userId)) {
            throw new DataNotFoundException("Пользователь с таким id не существует");
        }
        return repository.findAllByInitiatorId(userId, PageRequest.of(from, size)).stream()
                .map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto addNewEvent(Long userId, NewEventDto dto) {
        validateTime(dto.getEventDate());
        User user = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("Пользователь с таким id не существует"));
        Category category = categoryRepository.findById(dto.getCategory()).orElseThrow(
                () -> new DataNotFoundException("Категория не найдена"));
        Event event = EventMapper.toEventFromNewDto(dto, user, category);
        if (dto.getParticipantLimit() < 0) {
            throw new ValidationException("Лимит участников отрицательный");
        }
        return EventMapper.toEventFullDto(repository.save(event));
    }

    @Override
    public EventFullDto getEventByUserId(Long userId, Long eventId) {
        Event event = repository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new DataNotFoundException("События не существует"));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUserId(Long userId, Long eventId, UpdateEventAdminRequest dto) {
        Event event = repository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new DataNotFoundException("События не существует"));

        if (event.getState() == EventState.PUBLISHED || event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("События со статусом CANCELED или PENDING");
        }
        updateEventCommonFields(event, dto);
        Event eventSaved = repository.save(event);
        locationRepository.save(eventSaved.getLocation());
        return EventMapper.toEventFullDto(eventSaved);
    }

    @Override
    public List<ParticipationRequestDto> getAllRequestsByUserAndEvent(Long userId, Long eventId) {
        if (repository.existsByIdAndInitiatorId(eventId, userId)) {
            return requestRepository.findAllByEventId(eventId).stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        RequestStatus status = request.getStatus();
        if (status == CONFIRMED || status == REJECTED) {
            Event event = repository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                    () -> new DataNotFoundException("События не существует"));
            if (!event.getInitiator().getId().equals(userId)) {
                throw new ValidationException("Пользователь не может обновлять запросы к событию, автором которого он не является");
            }
            Integer participantLimit = event.getParticipantLimit();
            if (!event.getRequestModeration()) {
                throw new ValidationException("Событию не нужна модерация");
            }
            Long numberOfParticipants = requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);
            if ((numberOfParticipants >= participantLimit) || (participantLimit == 0)) {
                throw new AlreadyExistsException("В событии уже максимальное кол-во участников");
            }
            List<Request> requests = requestRepository.findByIdIn(request.getRequestIds());
            RequestStatus newStatus = request.getStatus();
            for (Request request1 : requests) {
                if (request1.getEvent().getId().equals(eventId)) {
                    if (participantLimit > numberOfParticipants) {
                        if (newStatus == CONFIRMED && request1.getStatus() != CONFIRMED) {
                            numberOfParticipants++;
                        }
                        request1.setStatus(newStatus);
                    } else {
                        request1.setStatus(REJECTED);
                    }
                } else {
                    throw new ValidationException("Запрос и событие не совпадают");
                }
            }
            requestRepository.saveAll(requests);
            List<Request> confirmedRequests = requestRepository.findAllByEventIdAndStatus(eventId, CONFIRMED);
            List<Request> rejectedRequests = requestRepository.findAllByEventIdAndStatus(eventId, REJECTED);

            List<ParticipationRequestDto> confirmedRequestDtos = confirmedRequests.stream()
                    .map(RequestMapper::toParticipationRequestDto)
                    .collect(Collectors.toList());

            List<ParticipationRequestDto> rejectedRequestDtos = rejectedRequests.stream()
                    .map(RequestMapper::toParticipationRequestDto)
                    .collect(Collectors.toList());
            return new EventRequestStatusUpdateResult(confirmedRequestDtos, rejectedRequestDtos);
        } else {
            throw new IllegalArgumentException("Доступны только статусы CONFIRMED или REJECTED");
        }
    }

    @Override
    public List<EventShortDto> getEventByFilter(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size) {
        dateValidate(rangeStart, rangeEnd);
        LocalDateTime dateStartSearch = LocalDateTime.now().plusSeconds(1L);
        LocalDateTime dateEndSearch = LocalDateTime.now().plusYears(99L);
        if (rangeStart != null) {
            dateStartSearch = rangeStart;
        }
        if (rangeEnd != null) {
            dateEndSearch = rangeEnd;
        }
        if (categories == null || categories.isEmpty()) {
            categories = categoryRepository.findAll().stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());
        }
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = repository.findPublicEvent(text, categories, paid, dateStartSearch, dateEndSearch, EventState.PUBLISHED, pageable);
        if (onlyAvailable) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() > getConfirmedRequests(e.getId()))
                    .collect(Collectors.toList());
        }
        LocalDateTime start = dateStartSearch;
        LocalDateTime end = dateEndSearch;
        List<EventShortDto> eventShorts = events.stream()
                .map(EventMapper::toEventShortDto)
                .peek(e -> {

                    e.setViews(viewsEvent(start, end, "/events/" + e.getId(), false));
                })
                .collect(Collectors.toList());
        if (sort.equals("VIEWS")) {
            eventShorts.sort(Comparator.comparing(EventShortDto::getViews));
        }
        return eventShorts;
    }

    @Override
    @Transactional
    public EventFullDto getPublicEventById(Long id) {
        Event event = repository.findById(id).orElseThrow(() -> new DataNotFoundException("Событие не существует"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new DataNotFoundException("Событие не опубликовано");
        }
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = viewsEvent(LocalDateTime.now().plusSeconds(1L), LocalDateTime.now().plusYears(99L), "/events/" + event.getId(), false);
        eventFullDto.setViews(views);
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
    }

    private void dateValidate(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new ValidationException("Дата окончания не может быть раньше даты начала");
            }
        }
    }
    private void setConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        List<ConfirmedRequest> confirmedRequests = requestRepository.findConfirmedRequest(eventIds);
        Map<Long, Long> confirmedRequestsMap = confirmedRequests.stream()
                .collect(Collectors.toMap(ConfirmedRequest::getEventId, ConfirmedRequest::getCount));
        events.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L)));
    }

    private void updateEventAdmin(Event event, UpdateEventAdminRequest eventDto) {
        updateEventCommonFields(event, eventDto);

        if (eventDto.getStateAction() != null) {
            if (event.getState().equals(EventState.PENDING)) {
                if (eventDto.getStateAction().equals(EventStateAction.REJECT_EVENT)) {
                    event.setState(EventState.CANCELED);
                }
                if (eventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)) {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
            } else {
                throw new AlreadyExistsException("Состояние неверное: " + event.getState());
            }
        }

        if (eventDto.getEventDate() != null && event.getState().equals(EventState.PUBLISHED)) {
            if (eventDto.getEventDate().isAfter(event.getPublishedOn().plusHours(1))) {
                event.setEventDate(eventDto.getEventDate());
            } else {
                throw new ValidationException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
        }
    }

    private void updateEventCommonFields(Event event, UpdateEventAdminRequest eventDto) {
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
            if (eventDto.getStateAction().equals(EventStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
        }
        if (eventDto.getAnnotation() != null && !eventDto.getAnnotation().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getCategory() != null) {
            Category category = getCategoryForEvent(eventDto.getCategory());
            event.setCategory(category);
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            if ((eventDto.getParticipantLimit() < 0)) {
                throw new ValidationException("Лимиты не верны");
            }
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(eventDto.getLocation());
        }
        if (eventDto.getEventDate() != null) {
            validateTime(eventDto.getEventDate());
            event.setEventDate(eventDto.getEventDate());
        }
    }

    private Category getCategoryForEvent(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Нет категории с id=" + id));
    }

    private void validateTime(LocalDateTime start) {
        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
        }
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);
    }

    private Long viewsEvent(LocalDateTime rangeStart, LocalDateTime rangeEnd, String uris, Boolean unique) {
        List<?> body = statisticsClient.getStat(rangeStart, rangeEnd, List.of(uris), unique);
        return body.size() > 0 ? ((HitOutcomeDto) body.get(0)).getHits() : 1L;
    }
}
