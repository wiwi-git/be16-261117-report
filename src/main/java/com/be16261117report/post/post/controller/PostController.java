package com.be16261117report.post.post.controller;

import com.be16261117report.post.post.dto.PostDto;
import com.be16261117report.post.post.dto.PostWriteReqBody;
import com.be16261117report.post.post.entity.Post;
import com.be16261117report.post.post.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 글 다건 조회
    @GetMapping
    @Transactional(readOnly = true)
    public List<PostDto> getItems() {
        return postService.findAll()
                .stream()
                .map(PostDto::new)
                .toList();
    }

    // 글 단건 조회
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PostDto getItem(@PathVariable int id) {
        return new PostDto(postService.findById(id));
    }

    // 글 작성
    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto write(@RequestBody @Valid PostWriteReqBody reqBody) {
        Post post = postService.write(reqBody.title(), reqBody.content());
        return new PostDto(post);
    }

    // 글 삭제
    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        postService.delete(id);
    }
}
