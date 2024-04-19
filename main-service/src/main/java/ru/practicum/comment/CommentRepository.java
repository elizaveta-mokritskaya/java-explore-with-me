package ru.practicum.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventIdOrderByCreatedOnDesc(Long eventId, Pageable pageable);

    Optional<Comment> findByEventIdAndAuthorId(Long eventId, Long userId);

    List<Comment> findTop10ByEventIdOrderByCreatedOnDesc(Long eventId);

    List<Comment> findAllByEventIdOrderByCreatedOnDesc(Long eventId);

    boolean existsCommentById(Long id);
}
