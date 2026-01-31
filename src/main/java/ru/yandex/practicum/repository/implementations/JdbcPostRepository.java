package ru.yandex.practicum.repository.implementations;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.interfaces.PostRepository;

import java.util.*;

@Repository("JdbcPostRepository")
@AllArgsConstructor
public class JdbcPostRepository implements PostRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Post> getPosts(String searchString, Integer pageSize, Integer currentPage) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder();
        //language=SQL
        sql.append("""
                SELECT ID,
                       TITLE,
                       CASE WHEN LENGTH(text) > 128
                               THEN SUBSTRING(text FROM 1 FOR 128) || '...'
                               ELSE text
                           END AS text,
                        likes_count as likesCount
                FROM posts
                """);

        sql.append(buildSearchCause(searchString, params));

        sql.append(" ORDER BY id DESC LIMIT :pageSize");

        params.put("pageSize", pageSize);

        if (currentPage > 1) {
            sql.append(" OFFSET :os");
            params.put("os", (currentPage - 1) * pageSize);
        }


        return jdbcTemplate.query(sql.toString(), params, new BeanPropertyRowMapper<>(Post.class));
    }

    @Override
    public Integer getPostsCount(String searchString) {
        //language=SQL
        String sqlQuery = "SELECT count(*) FROM posts";

        Map<String, Object> params = new HashMap<>();
        params.put("searchString", searchString);

        String searchCause = buildSearchCause(searchString, params);
        sqlQuery += searchCause;

        return jdbcTemplate.queryForObject(sqlQuery, params, Integer.class);
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

    private String buildSearchCause(String searchString, Map<String, Object> params) {
        StringBuilder searchStringBuilder = new StringBuilder();

        if (searchString != null && !searchString.isEmpty()) {
            searchStringBuilder.append(" WHERE");

            String[] words = searchString.split(" ");
            StringBuilder wordsSearch = new StringBuilder();
            List<String> hashtags = new ArrayList<>();
            for (String word : words) {
                if (word.equals(" ") || word.equals("")) {
                    continue;
                }

                if (word.startsWith("#")) {
                    hashtags.add(word.substring(1));
                } else {
                    wordsSearch.append(" ");
                    wordsSearch.append(word);
                }
            }
            if (wordsSearch.length() > 0) {
                wordsSearch.replace(0, 1, "%");
                wordsSearch.append("%");

                searchStringBuilder.append(" title LIKE :searchString");
                params.put("searchString", wordsSearch.toString());
            }

            if (wordsSearch.length() > 0 && hashtags.size() > 0) {
                searchStringBuilder.append(" AND");
            }

            if (hashtags.size() > 0) {
                searchStringBuilder.append(" id IN (SELECT post_id FROM post_tags WHERE tag IN (:hashtags) )");
                params.put("hashtags", hashtags);
            }

            return searchStringBuilder.toString();
        } else {
            return "";
        }
    }
}
