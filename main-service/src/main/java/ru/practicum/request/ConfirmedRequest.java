package ru.practicum.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmedRequest {
    private Long eventId;
    private Long count;
}
