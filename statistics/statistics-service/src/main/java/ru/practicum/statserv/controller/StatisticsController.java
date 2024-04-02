package ru.practicum.statserv.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.HitOutcomeDto;
import ru.practicum.statserv.exception.ValidationException;
import ru.practicum.statserv.service.StatisticsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statServ;
    private final String pattern = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    public void saveNewHit(@Valid @RequestBody HitDto hitDto) {
        log.info("Получен запрос на сохранение нового просмотра '{}'", hitDto);
        if ((hitDto.getApp() == null) || (hitDto.getUri() == null) || (hitDto.getIp() == null) || (hitDto.getTimestamp() == null)) {
            throw new ValidationException("Входные данные не корректны");
        }
        LocalDateTime dateTime = LocalDateTime.parse(hitDto.getTimestamp(), DateTimeFormatter.ofPattern(pattern));
        statServ.saveNewHit(hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), dateTime);
    }

    @GetMapping("/stats")
    public List<HitOutcomeDto> getStat(
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam String start,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam String end,
            @RequestParam(name = "uris", required = false) ArrayList<String> uris,
            @RequestParam(name = "unique", defaultValue = "false", required = false) boolean unique
    ) {
        log.info("Получен запрос на получение статистики");
        if ((start.isBlank()) || (end.isBlank())) {
            throw new ValidationException("Входные данные не корректны");
        }
        LocalDateTime dateTimeStart = LocalDateTime.parse(start, DateTimeFormatter.ofPattern(pattern));
        LocalDateTime dateTimeEnd = LocalDateTime.parse(end, DateTimeFormatter.ofPattern(pattern));
        if (dateTimeStart.isAfter(dateTimeEnd)) {
            throw new ValidationException("Входные данные не корректны");
        }
        if (!unique) {
            return statServ.getStatistic(dateTimeStart, dateTimeEnd, uris);
        } else {
            return statServ.getStatisticWithIp(dateTimeStart, dateTimeEnd, uris);
        }
    }
}
