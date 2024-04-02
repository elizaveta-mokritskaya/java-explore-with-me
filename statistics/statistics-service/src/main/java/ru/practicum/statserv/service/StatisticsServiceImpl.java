package ru.practicum.statserv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.statdto.HitOutcomeDto;
import ru.practicum.statserv.model.Hit;
import ru.practicum.statserv.repository.StatServRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    private final StatServRepository repository;

    @Override
    public void saveNewHit(String app, String uri, String ip, LocalDateTime timestamp) {
        repository.save(new Hit(null, app, uri, ip, timestamp));
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
