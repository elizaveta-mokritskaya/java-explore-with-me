package ru.practicum.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfirmedRequest {
    private Long eventId;
    private Long count;

    public ConfirmedRequest(Long eventId, Long count) {
        this.eventId = eventId;
        this.count = count;
    }
}