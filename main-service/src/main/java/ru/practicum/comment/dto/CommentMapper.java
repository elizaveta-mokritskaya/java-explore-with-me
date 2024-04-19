package ru.practicum.comment.dto;

import ru.practicum.comment.Comment;
import ru.practicum.event.Event;
import ru.practicum.user.User;

import java.time.LocalDateTime;

public class CommentMapper {
    public static CommentFullDto toCommentFullDto(Comment comment) {
        return new CommentFullDto(
                comment.getId(),
                comment.getCreatedOn(),
                comment.getEvent().getId(),
                comment.getAuthor().getId(),
                comment.getText()
        );
    }

    public static Comment toComment(User user, Event event, CommentDto dto) {
        return Comment.builder()
                .createdOn(LocalDateTime.now())
                .text(dto.getText())
                .event(event)
                .author(user)
                .build();
    }
}
