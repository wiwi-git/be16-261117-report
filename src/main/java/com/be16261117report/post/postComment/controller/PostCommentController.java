package com.be16261117report.post.postComment.controller;

import com.be16261117report.post.postComment.dto.PostCommentDto;
import com.be16261117report.post.postComment.dto.PostCommentWriteReqBody;
import com.be16261117report.post.postComment.entity.PostComment;
import com.be16261117report.post.postComment.service.PostCommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class PostCommentController {
    private final PostCommentService postCommentService;

    public PostCommentController(PostCommentService postCommentService) {
        this.postCommentService = postCommentService;
    }

    // 댓글 다건 조회
    @GetMapping
    public List<PostCommentDto> getItems(@PathVariable int postId) {
        return postCommentService.findAll(postId)
                .stream()
                .map(PostCommentDto::new)
                .toList();
    }

    // 댓글 단건 조회
    @GetMapping("/{id}")
    public PostCommentDto getItem(@PathVariable int postId, @PathVariable int id) {
        return new PostCommentDto(postCommentService.findById(postId, id));
    }

    // 댓글 작성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostCommentDto write(
            @PathVariable int postId,
            @RequestBody @Valid PostCommentWriteReqBody reqBody
    ) {
        PostComment postComment = postCommentService.write(postId, reqBody.content());
        return new PostCommentDto(postComment);
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int postId, @PathVariable int id) {
        postCommentService.delete(postId, id);
    }
}
