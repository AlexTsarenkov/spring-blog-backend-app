package ru.yandex.practicum.repository.interfaces;

import ru.yandex.practicum.model.PostImage;

import java.util.Optional;

public interface ImageRepository {
    void savePostImage(PostImage image);

    Optional<PostImage> getPostImageByPostId(Long postId);
}
