package ru.yandex.practicum.repository.implementations;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.interfaces.PostRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("JdbcPostRepository")
@AllArgsConstructor
public class JdbcPostRepository implements PostRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Post> getPosts(String searchString, Integer pageSize, Integer currentPage) {
        Map<String,Object> params = new HashMap<>();

        //language=SQL
        String query = "SELECT * FROM posts";

        if (searchString != null && !searchString.isEmpty()) {
            query += " WHERE search LIKE :searchString";
            params.put("searchString", "%" + searchString + "%");
        }

        query += " ORDER BY id DESC LIMIT :pageSize";

        params.put("pageSize", pageSize);

        if (currentPage > 1) {
            query += " OFFSET :os";
            params.put("os", (currentPage - 1) * pageSize );
        }


        return jdbcTemplate.query(query, params, new BeanPropertyRowMapper<>(Post.class));
    }

    @Override
    public Integer getPostsCount(String searchString) {
        //language=SQL
        String sqlQuery = "SELECT count(*) FROM posts";

        if (searchString != null && !searchString.isEmpty()) {
            sqlQuery += " WHERE search LIKE %:searchString%";
        }

        return jdbcTemplate.queryForObject(sqlQuery, Map.of("searchString", searchString), Integer.class);
    }

    @Override
    public Optional<Post> getPost(Long id) {
        //language=SQL
        String sqlQuery = "select * from posts where id = :id";

        return Optional.ofNullable(
                jdbcTemplate.queryForObject(sqlQuery, Map.of("id", id), new BeanPropertyRowMapper<>(Post.class)));
    }

    @Override
    public Post addPost(Post post) {
        //language=SQL
        String sqlQuery = """
                INSERT INTO posts(ID, TITLE, TEXT, LIKES_COUNT)
                VALUES (:id, :title, :text, :lk );
                """;

        Map<String, Object> params = Map.of(
                "id", post.getId(),
                "title", post.getTitle(),
                "text", post.getText(),
                "lk", post.getLikesCount()
        );

        jdbcTemplate.update(sqlQuery, params);

        return post;
    }

    @Override
    public Post updatePost(Post post) {
        //language=SQL
        String sqlQuery = """
                UPDATE posts 
                SET title = :title, text = :text 
                WHERE id = :id
                """;
        Map<String, Object> params = Map.of(
                "title", post.getTitle(),
                "text", post.getText(),
                "id", post.getId());

        jdbcTemplate.update(sqlQuery, params);

        return post;
    }

    @Override
    @Transactional //CASCADE deletion
    public void deletePost(Long id) {
        //language=SQL
        String sqlQuery = "DELETE FROM posts WHERE id = :id";
        jdbcTemplate.update(sqlQuery, Map.of("id", id));
    }

    @Override
    public void addLikeToPost(Long id) {
        //language=SQL
        String sqlQuery = "UPDATE posts SET likes_count = likes_count + 1 WHERE id = :id";
        jdbcTemplate.update(sqlQuery, Map.of("id", id));
    }
}
