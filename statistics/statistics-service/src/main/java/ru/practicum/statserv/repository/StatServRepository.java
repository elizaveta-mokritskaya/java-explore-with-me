package ru.practicum.statserv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.statdto.HitOutcomeDto;
import ru.practicum.statserv.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatServRepository extends JpaRepository<Hit, Long> {
//    @Query("select new ru.practicum.statdto.HitOutcomeDto(h.app, h.uri, count(h.id)) from Hit as h " +
//            "where h.timestamp between :start and :end " +
//            "group by h.app, h.uri order by count(h.id) desc ")
//    List<HitOutcomeDto> getByStartAndEnd(LocalDateTime start, LocalDateTime end);
//
//    @Query("select new ru.practicum.statdto.HitOutcomeDto(h.app, h.uri, count(h.id)) from Hit as h " +
//            "where h.timestamp between :start and :end and h.uri in :uris " +
//            "group by h.app, h.uri order by count(h.id) desc ")
//    List<HitOutcomeDto> getByStartAndEndAndUri(LocalDateTime start, LocalDateTime end, List<String> uris);
//
//    @Query("select new ru.practicum.statdto.HitOutcomeDto(h.app, h.uri, count(distinct(h.ip))) from Hit as h " +
//            "where h.timestamp >= :start and h.timestamp <= :end " +
//            "group by h.app, h.uri order by count(distinct(h.ip)) desc ")
//    List<HitOutcomeDto> getByStartAndEndAndIp(LocalDateTime start, LocalDateTime end);
//
//    @Query("select new ru.practicum.statdto.HitOutcomeDto(h.app, h.uri, count(distinct(h.ip))) from Hit as h " +
//            "where h.timestamp >= :start and h.timestamp <= :end and h.uri in :uris " +
//            "group by h.app, h.uri order by count(distinct(h.ip)) desc ")
//    List<HitOutcomeDto> getByStartAndEndAndUriAndIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.statdto.HitOutcomeDto(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit as h " +
            "where h.timestamp between :start and :end " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<HitOutcomeDto> getByStartAndEnd(LocalDateTime start, LocalDateTime end);


    @Query("select new ru.practicum.statdto.HitOutcomeDto(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit as h " +
            "where h.timestamp between :start and :end " +
            "and h.uri IN (:uris) " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<HitOutcomeDto> getByStartAndEndAndUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.statdto.HitOutcomeDto(h.app, h.uri, count(h.ip)) " +
            "from Hit as h " +
            "where h.timestamp between :start and :end " +
            "group by h.app, h.uri " +
            "order by count(h.ip) desc")
    List<HitOutcomeDto> getByStartAndEndAndIp(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.statdto.HitOutcomeDto(h.app, h.uri, count(h.ip)) " +
            "from Hit as h " +
            "where h.timestamp between :start and :end " +
            "and h.uri in :uris " +
            "group by h.app, h.uri " +
            "order by count(h.ip) desc")
    List<HitOutcomeDto> getByStartAndEndAndUriAndIp(LocalDateTime start,
                                   LocalDateTime end,
                                    List<String> uris);
}
