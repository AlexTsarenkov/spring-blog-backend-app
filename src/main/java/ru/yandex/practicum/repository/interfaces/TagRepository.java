package ru.yandex.practicum.repository.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TagRepository {
    void saveTagsForPost(Long postId, List<String> tags);

    List<String> getTagsForPost(Long postId);

    Map<Long, Set<String>> getTagsForMultiplePostIds(List<Long> postIds);
}
