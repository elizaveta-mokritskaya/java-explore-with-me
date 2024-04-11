package ru.practicum.statserv.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.HitOutcomeDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {

    void saveNewHit(HitDto hitDto);

    //List<HitOutcomeDto> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris);

    //List<HitOutcomeDto> getStatisticWithIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Transactional(readOnly = true)
    List<HitOutcomeDto> getStatistic(String startDate, String endDate, List<String> uris);

    @Transactional(readOnly = true)
    List<HitOutcomeDto> getStatisticWithIp(String startDate, String endDate, List<String> uris);
}
