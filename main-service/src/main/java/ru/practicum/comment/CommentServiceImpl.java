package ru.practicum.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentFullDto;
import ru.practicum.comment.dto.CommentMapper;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventState;
import ru.practicum.exception.AccessException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.request.Request;
import ru.practicum.request.RequestRepository;
import ru.practicum.request.RequestStatus;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public CommentFullDto addComment(Long userId, Long eventId, CommentDto commentDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new DataNotFoundException("Событие не найдено"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Событие еще не опубликовано");
        }
        if (!Objects.equals(user.getId(), event.getInitiator().getId())) {
            List<Request> requests = requestRepository.findAllByEventIdAndStatusAndRequesterId(eventId,
                    RequestStatus.CONFIRMED, userId);
            if (requests.isEmpty()) {
                throw new AccessException("Вы не участник и не автор события");
            }
        }
        Optional<Comment> foundComment = repository.findByEventIdAndAuthorId(eventId, userId);
        if (foundComment.isPresent()) {
            throw new AccessException("Можно оставить только один комментарий");
        }
        return CommentMapper.toCommentFullDto(repository.save(CommentMapper.toComment(user, event, commentDto)));
    }

    @Override
    @Transactional
    public CommentFullDto updateComment(Long commentId, Long userId, CommentDto commentDto) {
        Comment comment = repository.findById(commentId).orElseThrow(() -> new DataNotFoundException("Комментарий не найден"));
        if (!userRepository.existsUserById(userId)) {
            throw new DataNotFoundException("Пользователь не найден");
        }
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new AccessException("Пользователь не является автором комментария");
        }
        String newText = commentDto.getText();
        if (newText != null && !newText.isEmpty()) {
            comment.setText(newText);
        }
        return CommentMapper.toCommentFullDto(comment);
    }

    @Override
    public void deleteCommentById(Long commentId, Long userId) {
        Comment comment = repository.findById(commentId).orElseThrow(() -> new DataNotFoundException("Комментарий не найден"));
        if (!userRepository.existsUserById(userId)) {
            throw new DataNotFoundException("Пользователь не найден");
        }
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new AccessException("Пользователь не является автором комментария");
        }
        repository.deleteById(commentId);
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        if (!repository.existsCommentById(commentId)) {
            throw new DataNotFoundException("Комментарий не найден");
        }
        repository.deleteById(commentId);
    }

    @Override
    @Transactional
    public List<CommentFullDto> getAllCommentsByEventId(Long eventId, int from, int size) {
        if (!eventRepository.existsById(eventId)) {
            throw new DataNotFoundException("Событие не найдено");
        }
        Pageable pageable = PageRequest.of(from, size);
        List<Comment> comments = repository.findAllByEventIdOrderByCreatedOnDesc(eventId, pageable);

        return comments.stream().map(CommentMapper::toCommentFullDto).collect(Collectors.toList());
    }
}
