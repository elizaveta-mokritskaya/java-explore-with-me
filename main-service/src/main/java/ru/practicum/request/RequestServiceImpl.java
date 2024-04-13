package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventState;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new DataNotFoundException("Пользователь с таким id не существует");
        }
        return repository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addNewRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new DataNotFoundException("Событие не найдено"));
        User requestor = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        validateRequest(requestor, event);
        Request request = RequestMapper.toRequest(requestor, event);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        request = repository.save(request);
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return RequestMapper.toParticipationRequestDto(request);
    }

    @Override
    public ParticipationRequestDto updateRequest(Long userId, Long requestId) {
        Request request = repository.findById(requestId).orElseThrow(() -> new DataNotFoundException("Запрос не найден"));
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toParticipationRequestDto(repository.save(request));
    }

    private void validateRequest(User requestor, Event event) {
        if (requestor.getId().equals(event.getInitiator().getId())) {
            throw new AlreadyExistsException("Инициатор события не может быть его участником");
        }
        List<Request> requests = repository.findAllByEventIdAndRequesterId(event.getId(), requestor.getId());
        if (!requests.isEmpty()) {
            throw new AlreadyExistsException("Заявка уже существует");
        }
        if (!(event.getState() == EventState.PUBLISHED)) {
            throw new AlreadyExistsException("Заявку можно создать только для опубликованного события");
        }
        if (event.getParticipantLimit() > 0) {
            Long participants = repository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            Integer limit = event.getParticipantLimit();
            if (participants >= limit) {
                throw new AlreadyExistsException("Достигнуто максимальное количество заявок");
            }
        }
    }
}
