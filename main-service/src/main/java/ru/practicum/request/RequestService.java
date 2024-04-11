package ru.practicum.request;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    ParticipationRequestDto addNewRequest(Long userId, Long eventId);

    ParticipationRequestDto updateRequest(Long userId, Long requestId);
}
