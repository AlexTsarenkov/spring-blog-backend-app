package ru.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImage {
    private Long postId;
    private String fileName;
    private String contentType;
    private Long size;
    private byte[] data;
}
