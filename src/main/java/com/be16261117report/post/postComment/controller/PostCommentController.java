package com.be16261117report.post.postComment.controller;

import com.be16261117report.post.post.entity.Post;
import com.be16261117report.post.post.service.PostService;
import com.be16261117report.post.postComment.dto.PostCommentDto;
import com.be16261117report.post.postComment.dto.PostCommentWriteReqBody;
import com.be16261117report.post.postComment.entity.PostComment;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 댓글은 글(Post)을 통해서만 다룬다.
// 액션의 트랜잭션 안에서 글을 조회하므로 글은 영속 상태이고,
// 댓글 컬렉션을 바꾸면 커밋 시점에 더티 체킹으로 DB에 반영된다.
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class PostCommentController {
    private final PostService postService;

    public PostCommentController(PostService postService) {
        this.postService = postService;
    }

    // 댓글 다건 조회
    @GetMapping
    @Transactional(readOnly = true)
    public List<PostCommentDto> getItems(@PathVariable int postId) {
        Post post = postService.findById(postId);

        return postService.findComments(post)
                .stream()
                .map(PostCommentDto::new)
                .toList();
    }

    // 댓글 단건 조회
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PostCommentDto getItem(@PathVariable int postId, @PathVariable int id) {
        Post post = postService.findById(postId);
        PostComment postComment = postService.findCommentById(post, id);

        return new PostCommentDto(postComment);
    }

    // 댓글 작성
    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public PostCommentDto write(
            @PathVariable int postId,
            @RequestBody @Valid PostCommentWriteReqBody reqBody
    ) {
        Post post = postService.findById(postId);
        PostComment postComment = postService.writeComment(post, reqBody.content());

        return new PostCommentDto(postComment);
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int postId, @PathVariable int id) {
        Post post = postService.findById(postId);
        PostComment postComment = postService.findCommentById(post, id);

        postService.deleteComment(post, postComment);
    }
}
