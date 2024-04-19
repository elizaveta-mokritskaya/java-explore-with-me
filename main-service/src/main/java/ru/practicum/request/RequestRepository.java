package ru.practicum.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("select new ru.practicum.request.ConfirmedRequest(r.event.id, count(distinct r)) " +
            "from Request r " +
            "where r.status = 'CONFIRMED' and r.event.id in :eventsIds " +
            "group by r.event.id")
    List<ConfirmedRequest> findConfirmedRequest(List<Long> eventsIds);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findByIdIn(List<Long> requestIds);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByRequesterId(Long userId);

    Boolean existsByRequesterId(Long userId);

    List<Request> findAllByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<Request> findAllByEventIdAndStatusAndRequesterId(Long eventId, RequestStatus confirmed, Long userId);
}
