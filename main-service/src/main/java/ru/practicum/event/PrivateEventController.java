package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
@Slf4j
public class PrivateEventController {
    private final EventService service;

    @GetMapping
    public List<EventShortDto> getAllEventsByUserId(@PathVariable Long userId,
                                                    @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                                    @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        return service.getAllEventsByUserId(userId, from / size, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addNewEvent(@PathVariable Long userId,
                                    @Valid @RequestBody NewEventDto dto) {
        return service.addNewEvent(userId, dto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUserId(@PathVariable Long userId,
                                         @PathVariable Long eventId) {
        return service.getEventByUserId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUserId(@PathVariable Long userId,
                                            @PathVariable Long eventId,
                                            @Valid @RequestBody UpdateEventAdminRequest dto) {
        return service.updateEventByUserId(userId, eventId, dto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getAllRequestsByUserAndEvent(@PathVariable Long userId,
                                                                      @PathVariable Long eventId) {
        return service.getAllRequestsByUserAndEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestStatus(@PathVariable Long userId,
                                                                   @PathVariable Long eventId,
                                                                   @RequestBody EventRequestStatusUpdateRequest request) {
        return service.updateEventRequestStatus(userId, eventId, request);
    }
}
