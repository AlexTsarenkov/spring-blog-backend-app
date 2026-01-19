package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exception.PostNotFoundException;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.model.dto.PostListDto;
import ru.yandex.practicum.repository.interfaces.CommentRepository;
import ru.yandex.practicum.repository.interfaces.PostRepository;
import ru.yandex.practicum.repository.interfaces.TagRepository;
import ru.yandex.practicum.utils.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;

    public PostService(@Qualifier("JdbcPostRepository") PostRepository postRepository,
                       @Qualifier("JdbcTagRepository") TagRepository tagRepository,
                       @Qualifier("JdbcCommentRepository") CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Post addPost(Post post) {
        Post postToAdd = Post.builder()
                .id(Utility.getIdForEntity())
                .text(post.getText())
                .title(post.getTitle())
                .tags(post.getTags())
                .likesCount((short) 0)
                .commentsCount((short) 0)
                .build();

        postRepository.addPost(postToAdd);

        if (postToAdd.getTags() != null && postToAdd.getTags().size() > 0) {
            tagRepository.saveTagsForPost(postToAdd.getId(), postToAdd.getTags());
        }

        return postToAdd;
    }

    public Post getPost(Long id) {
        Post post = postRepository.getPost(id).orElseThrow(() -> new PostNotFoundException("Post not found"));
        List<String> tags = tagRepository.getTagsForPost(id);

        post.setTags(tags);
        post.setCommentsCount(commentRepository.getPostLikeCount(post.getId()));

        return post;
    }

    public PostListDto getPosts(String searchString, Integer pageSize, Integer page) {
        PostListDto postListDto = new PostListDto();

        List<Post> posts = postRepository.getPosts(searchString, pageSize, page);
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        Map<Long, Set<String>> tagsByPostId = tagRepository.getTagsForMultiplePostIds(postIds);

        posts.stream()
                .map(post -> {
                    Set<String> tags = tagsByPostId.get(post.getId());
                    if (tags != null) {
                        post.setTags(new ArrayList<>(tags));
                    }
                    return post;
                })
                .toList();

        Integer postsCount = postRepository.getPostsCount(searchString);
        Integer pages = (postsCount + pageSize - 1) / pageSize;

        postListDto.setPosts(posts);
        postListDto.setHasPrev(pages > 1 && page != 1);
        postListDto.setHasNext(pages > 1 && page != pages);
        postListDto.setLastPage(pages);

        return postListDto;
    }

    public void deletePost(Long id) {
        postRepository.deletePost(id);
    }

    @Transactional
    public Post updatePost(Long id, Post post) {
        Post postToUpdate = Post.builder()
                .id(id)
                .title(post.getTitle())
                .text(post.getText())
                .tags(post.getTags())
                .likesCount(post.getLikesCount())
                .build();

        postRepository.updatePost(postToUpdate);

        if (post.getTags() != null && post.getTags().size() > 0) {
            tagRepository.saveTagsForPost(id, postToUpdate.getTags());
        }

        return postToUpdate;
    }

    public void addLike(Long postId) {
        postRepository.addLikeToPost(postId);
    }
}
