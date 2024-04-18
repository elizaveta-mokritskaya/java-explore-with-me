package ru.practicum.comment;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentFullDto;

import java.util.List;

public interface CommentService {
    CommentFullDto addComment(Long userId, Long eventId, CommentDto commentDto);

    CommentFullDto updateComment(Long commentId, Long userId, CommentDto commentDto);

    void deleteCommentById(Long commentId, Long userId);

    void deleteCommentByAdmin(Long commentId);


    List<CommentFullDto> getAllCommentsByEventId(Long eventId, int from, int size);
}

