package ru.yandex.practicum.repository.implementations;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.PostImage;
import ru.yandex.practicum.repository.interfaces.ImageRepository;

import java.util.Map;
import java.util.Optional;

@Repository("JdbcImageRepository")
@AllArgsConstructor
public class JdbcImageRepository implements ImageRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void savePostImage(PostImage image) {
        //language=SQL
        String sqlQuery = """
                INSERT INTO post_image(POST_ID, FILE_NAME, CONTENT_TYPE, FILE_SIZE, FILE_DATA)
                VALUES (:id, :fn, :ct, :fs, :fd)
                ON CONFLICT(POST_ID)
                DO UPDATE SET
                    FILE_NAME = excluded.FILE_NAME,
                    CONTENT_TYPE = excluded.CONTENT_TYPE,
                    FILE_SIZE = excluded.FILE_SIZE,
                    FILE_DATA = excluded.FILE_DATA;
                """;

        Map<String, Object> params = Map.of(
                "id", image.getPostId(),
                "fn", image.getFileName(),
                "ct", image.getContentType(),
                "fs", image.getSize(),
                "fd", image.getData()
        );

        jdbcTemplate.update(sqlQuery, params);
    }

    @Override
    public Optional<PostImage> getPostImageByPostId(Long postId) {
        //language=SQL
        String sqlQuery = """
                SELECT POST_ID as postId,
                       FILE_NAME as fileName,
                       CONTENT_TYPE as contentType,
                       FILE_SIZE as size,
                       FILE_DATA as data
                FROM post_image
                WHERE POST_ID = :id
                LIMIT 1
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sqlQuery, Map.of("id", postId),
                    new BeanPropertyRowMapper<>(PostImage.class)));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }
}
