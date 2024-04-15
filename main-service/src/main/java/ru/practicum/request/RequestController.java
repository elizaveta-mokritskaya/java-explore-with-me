package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")
@Slf4j
public class RequestController {
    private final RequestService service;


    @GetMapping
    public List<ParticipationRequestDto> getRequestsByUserId(@PathVariable Long userId) {
        return service.getRequestsByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addNewRequest(@PathVariable Long userId,
                                                 @RequestParam Long eventId) {
        return service.addNewRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto updateRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {

        return service.updateRequest(userId, requestId);
    }
}
