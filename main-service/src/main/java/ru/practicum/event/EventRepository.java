package ru.practicum.event;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("select e " +
            "from Event e " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.category " +
            "where e.eventDate > :rangeStart " +
            "and (e.category.id in :categories or :categories is null) " +
            "and (e.initiator.id in :users or :users is null) " +
            "and (e.state in :states or :states is null)")
    List<Event> findAllForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                LocalDateTime rangeStart, PageRequest pageRequest);

    List<Event> findAllByInitiatorId(Long userId, PageRequest of);

    Optional<Event> findByIdAndInitiatorId(Long id, Long userId);

    Boolean existsByIdAndInitiatorId(Long id, Long userId);

    @Query("select e " +
            "from Event e " +
            "where (lower(e.annotation) like lower(concat('%', :text, '%')) or lower(e.description) like lower(concat('%', :text, '%')) or :text is null) " +
            "and (e.category.id in :categories or :categories is null) " +
            "and (e.paid=:paid or :paid is null) " +
            "and (e.eventDate between :rangeStart and :rangeEnd) " +
            "and (e.state = :state) " +
            "order by e.eventDate")
    List<Event> findPublicEvent(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                LocalDateTime rangeEnd, EventState state, Pageable pageable);

    Boolean existsByCategoryId(Long categoryId);
}
