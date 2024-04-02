package ru.practicum.statserv.service;

import ru.practicum.statdto.HitOutcomeDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {

    void saveNewHit(String app, String uri, String ip, LocalDateTime timestamp);

    List<HitOutcomeDto> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris);

    List<HitOutcomeDto> getStatisticWithIp(LocalDateTime start, LocalDateTime end, List<String> uris);
}
