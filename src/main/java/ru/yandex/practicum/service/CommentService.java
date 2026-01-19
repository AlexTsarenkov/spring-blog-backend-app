package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.repository.interfaces.CommentRepository;
import ru.yandex.practicum.utils.Utility;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;

    public CommentService(@Qualifier("JdbcCommentRepository") CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> getComments(Long postId) {
        return commentRepository.getComments(postId);
    }

    public Comment createComment(Long postId, Comment comment) {
        Comment commentToAdd = Comment.builder()
                .id(Utility.getIdForEntity())
                .postId(postId)
                .text(comment.getText())
                .build();
        return commentRepository.createComment(commentToAdd);
    }

    public Comment updateComment(Long postId, Long commentId, Comment comment) {
        Comment commentToUpdate = Comment.builder()
                .id(commentId)
                .postId(comment.getPostId())  //на фронте ошибка, в postId ид комента
                .text(comment.getText())
                .build();
        return commentRepository.updateComment(commentToUpdate);
    }

    public void deleteComment(Long postId, Long commentId) {
        commentRepository.deleteComment(postId, commentId);
    }
}
