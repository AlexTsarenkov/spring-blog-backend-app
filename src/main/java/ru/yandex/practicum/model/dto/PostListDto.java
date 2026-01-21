package ru.yandex.practicum.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.model.Post;

import java.util.List;

@Data
@NoArgsConstructor
public class PostListDto {
    private List<Post> posts;
    private Boolean hasPrev;
    private Boolean hasNext;
    private Integer lastPage;
}
