package ru.practicum.statserv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.HitOutcomeDto;
import ru.practicum.statserv.model.Hit;
import ru.practicum.statserv.repository.StatServRepository;

import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    private final StatServRepository repository;
    private final String pattern = "yyyy-MM-dd HH:mm:ss";
    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void saveNewHit(HitDto hitDto) {
        log.debug("форматируем дату");
        LocalDateTime dateTime = LocalDateTime.parse(hitDto.getTimestamp(), FORMATTER);
        log.debug("попытка сохранить Hit");
        repository.save(new Hit(null, hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), dateTime));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HitOutcomeDto> getStatistic(String startDate, String endDate, List<String> uris) {
        log.debug("форматируем дату start");
        LocalDateTime start = LocalDateTime.parse(decode(startDate), FORMATTER);
        log.debug("форматируем дату end");
        LocalDateTime end = LocalDateTime.parse(decode(endDate), FORMATTER);
        if (uris == null) {
            log.info("repository.getByStartAndEnd(start, end);");
            return repository.getByStartAndEnd(start, end);
        } else {
            log.info("repository.getByStartAndEndAndUri(start, end, uris);");
            return repository.getByStartAndEndAndUri(start, end, uris);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HitOutcomeDto> getStatisticWithIp(String startDate, String endDate, List<String> uris) {
        log.info("форматируем дату start");
        LocalDateTime start = LocalDateTime.parse(decode(startDate), FORMATTER);
        log.info("форматируем дату end");
        LocalDateTime end = LocalDateTime.parse(decode(endDate), FORMATTER);
        if (uris == null) {
            return repository.getByStartAndEndAndIp(start, end);
        } else {
            return repository.getByStartAndEndAndUriAndIp(start, end, uris);
        }
    }

    private String decode(String string) {
        return URLDecoder.decode(string);
    }
}
