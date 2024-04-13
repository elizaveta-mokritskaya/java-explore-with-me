package ru.practicum.statserv.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveNewHit(@Valid @RequestBody HitDto hitDto) {
        log.info("Получен запрос на сохранение нового просмотра '{}'", hitDto);
        if ((hitDto.getApp() == null) || (hitDto.getUri() == null) || (hitDto.getIp() == null) || (hitDto.getTimestamp() == null)) {
            throw new ValidationException("Входные данные не корректны");
        }
        statServ.saveNewHit(hitDto);
    }

    @GetMapping("/stats")
    public List<HitOutcomeDto> getStat(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(name = "uris", required = false) ArrayList<String> uris,
            @RequestParam(name = "unique", defaultValue = "false", required = false) boolean unique
    ) {
        log.info("Получен запрос на получение статистики");
        log.info("!!!!!!!!!!!!!!!!!!{}", start);
        if ((start.isBlank()) || (end.isBlank())) {
            throw new ValidationException("Входные данные не корректны");
        }
        LocalDateTime dateTimeStart = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime dateTimeEnd = LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (dateTimeStart.isAfter(dateTimeEnd)) {
            throw new ValidationException("Входные данные не корректны.");
        }
        if (unique) {
            return statServ.getStatistic(start, end, uris);
        } else {
            return statServ.getStatisticWithIp(start, end, uris);
        }
    }
}
