package ru.yandex.practicum.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.model.dto.PostListDto;
import ru.yandex.practicum.service.CommentService;
import ru.yandex.practicum.service.ImageService;
import ru.yandex.practicum.service.PostService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/posts")
@AllArgsConstructor
public class PostController {
    private final PostService postService;
    private final ImageService imageService;
    private final CommentService commentService;

    @PostMapping
    @ResponseBody
    @CrossOrigin("http://localhost")
    public Post addPost(@RequestBody Post post) {
        return postService.addPost(post);
    }

    @GetMapping
    @ResponseBody
    @CrossOrigin("http://localhost")
    public PostListDto getAllPosts(@RequestParam(name = "search") String search,
                                @RequestParam(name = "pageNumber") int pageNumber,
                                @RequestParam(name = "pageSize") int pageSize) {
        return postService.getPosts(search, pageSize, pageNumber);
    }

    @GetMapping(value = "/{postId}")
    @ResponseBody
    @CrossOrigin("http://localhost")
    public Post getPost(@PathVariable(name = "postId") Long postId) {
        return postService.getPost(postId);
    }

    @PutMapping(value = "/{postId}")
    @ResponseBody
    @CrossOrigin("http://localhost")
    public Post updatePost(@PathVariable(name = "postId") Long postId, @RequestBody Post post) {
        return postService.updatePost(postId, post);
    }

    @DeleteMapping(value = "/{postId}")
    @CrossOrigin("http://localhost")
    public void deletePost(@PathVariable(name = "postId") Long postId) {
        postService.deletePost(postId);
    }

    @PostMapping(value = "/{post_id}/likes")
    @CrossOrigin("http://localhost")
    public void likePost(@PathVariable(name = "post_id") Long postId) {
        postService.addLike(postId);
    }

    @GetMapping(value = "/{postId}/comments")
    @ResponseBody
    @CrossOrigin("http://localhost")
    public List<Comment> getComments(@PathVariable(name = "postId") Long postId) {
        return commentService.getComments(postId);
    }

    @PostMapping(value = "/{postId}/comments")
    @ResponseBody
    @CrossOrigin("http://localhost")
    public Comment createComment(@PathVariable(name = "postId") Long postId, @RequestBody Comment comment) {
        return commentService.createComment(postId, comment);
    }

    @PutMapping(value = "/{postId}/comments/{commentId}")
    @ResponseBody
    @CrossOrigin("http://localhost")
    public Comment updateComment(@PathVariable(name = "postId") Long postId,
                                 @PathVariable(name = "commentId") Long commentId,
                                 @RequestBody Comment comment) {
        return commentService.updateComment(postId, commentId, comment);
    }

    @DeleteMapping(value = "/{postId}/comments/{commentId}")
    @ResponseBody
    @CrossOrigin("http://localhost")
    public void deleteComment(@PathVariable(name = "postId") Long postId,
                              @PathVariable(name = "commentId") Long commentId) {
        commentService.deleteComment(postId, commentId);
    }


    @PutMapping(value = "/{postId}/image")
    @CrossOrigin("http://localhost")
    public void addPostImage(@PathVariable(name = "postId") Long postId, @RequestParam("image") MultipartFile image) {
        try {
            imageService.setImageForPost(postId, image);
        } catch (IOException e) {
            //TODO: log
        }
    }

    @GetMapping(value = "/{postId}/image")
    @CrossOrigin("http://localhost")
    public ResponseEntity<Resource> getPostImage(@PathVariable(name = "postId") Long postId) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(imageService.getImageForPost(postId));
    }
}
