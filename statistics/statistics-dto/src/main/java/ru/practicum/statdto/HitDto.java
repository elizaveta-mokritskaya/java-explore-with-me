package ru.practicum.statdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HitDto {

    private String app;
    private String uri;
    private String ip;
    private String timestamp;

}
