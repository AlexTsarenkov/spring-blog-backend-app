package ru.yandex.practicum.repository.interfaces;

import ru.yandex.practicum.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Comment createComment(Comment comment);

    Comment updateComment(Comment comment);

    void deleteComment(Long postId, Long commentId);

    Optional<Comment> getComment(Long postId, Long commentId);

    List<Comment> getComments(Long postId);

    Short getPostLikeCount(Long id);
}
