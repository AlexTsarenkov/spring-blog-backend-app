package ru.yandex.practicum.repository.implementations;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.repository.interfaces.TagRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository("JdbcTagRepository")
@AllArgsConstructor
public class JdbcTagRepository implements TagRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void saveTagsForPost(Long postId, List<String> tags) {
        //language=SQL
        String deleteQuery = "DELETE FROM post_tags WHERE post_id = ?";
        //language=SQL
        String insertQuery = "INSERT INTO post_tags (post_id, tag) VALUES (?, ?)";

        //first delete old tags, if it update case
        jdbcTemplate.getJdbcOperations().update(deleteQuery, postId);

        //set new tags
        jdbcTemplate.getJdbcOperations().batchUpdate(insertQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, postId);
                ps.setString(2, tags.get(i));
            }

            @Override
            public int getBatchSize() {
                return tags.size();
            }
        });
    }


    @Override
    public List<String> getTagsForPost(Long postId) {
        //language=SQL
        String selectQuery = "SELECT tag FROM post_tags WHERE post_id = :postId";
        return jdbcTemplate.queryForList(selectQuery, Map.of("postId", postId), String.class);
    }

    @Override
    public Map<Long, Set<String>> getTagsForMultiplePostIds(List<Long> postIds) {
        //language=SQL
        String selectQuery = "SELECT post_id, tag FROM post_tags WHERE post_id IN (:postIds)";

        try {
            return jdbcTemplate.query(selectQuery, Map.of("postIds", postIds), rs -> {
                Map<Long, Set<String>> result = new HashMap<>();

                while (rs.next()) {
                    Long postId = rs.getLong("post_id");
                    String tag = rs.getString("tag");

                    if (!result.containsKey(postId)) {
                        Set<String> tagSet = new HashSet<>();
                        tagSet.add(tag);
                        result.put(postId, tagSet);
                    } else {
                        result.get(postId).add(tag);
                    }
                }

                return result;
            });
        } catch (DataAccessException ex) {
            return Collections.emptyMap();
        }
    }
}
