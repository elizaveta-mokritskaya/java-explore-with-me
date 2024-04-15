package ru.practicum.event;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventFullDto> getEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByIdAdmin(Long eventId, UpdateEventAdminRequest dto);

    List<EventShortDto> getAllEventsByUserId(Long userId, int from, int size);

    EventFullDto addNewEvent(Long userId, NewEventDto dto);

    EventFullDto getEventByUserId(Long userId, Long eventId);

    EventFullDto updateEventByUserId(Long userId, Long eventId, UpdateEventAdminRequest dto);

    List<ParticipationRequestDto> getAllRequestsByUserAndEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<EventShortDto> getEventByFilter(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size);

    EventFullDto getPublicEventById(Long id);
}
