package ru.practicum.statserv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.HitOutcomeDto;
import ru.practicum.statserv.model.Hit;
import ru.practicum.statserv.repository.StatServRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    private final StatServRepository repository;
    private final String pattern = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void saveNewHit(HitDto hitDto) {
        LocalDateTime dateTime = LocalDateTime.parse(hitDto.getTimestamp(), DateTimeFormatter.ofPattern(pattern));
        repository.save(new Hit(null, hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), dateTime));
    }

    @Override
    public List<HitOutcomeDto> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris == null) {
            return repository.getByStartAndEnd(start, end);
        } else {
            return repository.getByStartAndEndAndUri(start, end, uris);
        }
    }

    @Override
    public List<HitOutcomeDto> getStatisticWithIp(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris == null) {
            return repository.getByStartAndEndAndIp(start, end);
        } else {
            return repository.getByStartAndEndAndUriAndIp(start, end, uris);
        }
    }
}
