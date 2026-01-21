package ru.yandex.practicum.repository.interfaces;

import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    List<Post> getPosts(String searchString,  Integer pageSize, Integer currentPage);

    Integer getPostsCount(String searchString);

    Optional<Post> getPost(Long id);

    Post addPost(Post post);

    Post updatePost(Post post);

    void deletePost(Long id);

    void addLikeToPost(Long id);
}
