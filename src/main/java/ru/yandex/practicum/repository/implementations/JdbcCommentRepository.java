package ru.yandex.practicum.repository.implementations;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.repository.interfaces.CommentRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("JdbcCommentRepository")
@AllArgsConstructor
public class JdbcCommentRepository implements CommentRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Comment createComment(Comment comment) {
        //language=SQL
        String sqlQuery = "INSERT INTO comments (id, post_id, text) VALUES (:id, :postId, :text)";

        Map<String, Object> params = Map.of(
                "id", comment.getId(),
                "postId", comment.getPostId(),
                "text", comment.getText()
        );

        jdbcTemplate.update(sqlQuery, params);

        return comment;
    }

    @Override
    public Comment updateComment(Comment comment) {
        //language=SQL
        String sqlQuery = "UPDATE comments SET text = :text WHERE id = :id AND post_id = :postId";

        Map<String, Object> params = Map.of(
                "id", comment.getId(),
                "postId", comment.getPostId(),
                "text", comment.getText()
        );

        jdbcTemplate.update(sqlQuery, params);

        return comment;
    }

    @Override
    public void deleteComment(Long postId, Long commentId) {
        //language=SQL
        String sqlQuery = "DELETE FROM comments WHERE post_id = :postId AND id = :commentId";

        Map<String, Object> params = Map.of("postId", postId, "commentId", commentId);

        jdbcTemplate.update(sqlQuery, params);
    }

    @Override
    public Optional<Comment> getComment(Long postId, Long commentId) {
        //language=SQL
        String sqlQuery = """
                SELECT ID,
                    TEXT,
                    POST_ID as postId
                FROM comments
                WHERE post_id = :postId
                AND id = :commentId
                """;
        Map<String, Object> params = Map.of("postId", postId, "commentId", commentId);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, params, new BeanPropertyRowMapper<>(Comment.class)));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Comment> getComments(Long postId) {
        //language=SQL
        String sqlQuery = "SELECT ID,TEXT,POST_ID as postId FROM comments WHERE post_id = :postId";

        return jdbcTemplate.query(sqlQuery, Map.of("postId", postId), new BeanPropertyRowMapper<>(Comment.class));
    }

    @Override
    public Short getPostLikeCount(Long id) {
        //language=SQL
        String sqlQuery = "SELECT COUNT(*) FROM comments WHERE id = :id";
        return jdbcTemplate.queryForObject(sqlQuery, Map.of("id", id), Short.class);
    }
}
