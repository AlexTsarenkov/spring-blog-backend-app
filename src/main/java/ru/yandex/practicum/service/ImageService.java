package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.model.PostImage;
import ru.yandex.practicum.repository.interfaces.ImageRepository;

import java.io.IOException;

@Service
public class ImageService {
    private final ImageRepository imageRepository;

    public ImageService(@Qualifier("JdbcImageRepository") ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public void setImageForPost(Long postId, MultipartFile image) throws IOException {
        PostImage postImage = PostImage.builder()
                .postId(postId)
                .fileName(image.getOriginalFilename())
                .contentType(image.getContentType())
                .size(image.getSize())
                .data(image.getBytes())
                .build();
        imageRepository.savePostImage(postImage);
    }

    public Resource getImageForPost(Long postId) {
        PostImage image = imageRepository.getPostImageByPostId(postId)
                .orElseGet(() -> {
                    ClassPathResource resource = new ClassPathResource("images/image404.jpg");
                    try {
                        return PostImage.builder()
                                .postId(postId)
                                .data(resource.getContentAsByteArray())
                                .build();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        return new ByteArrayResource(image.getData());
    }
}
