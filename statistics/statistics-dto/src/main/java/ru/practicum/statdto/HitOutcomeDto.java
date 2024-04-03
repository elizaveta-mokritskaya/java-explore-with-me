package ru.practicum.statdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HitOutcomeDto {
    private String app;
    private String uri;
    private long hits;
}
