package com.be16261117report.post.postComment.service;

import com.be16261117report.post.post.entity.Post;
import com.be16261117report.post.post.service.PostService;
import com.be16261117report.post.postComment.entity.PostComment;
import com.be16261117report.post.postComment.repository.PostCommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostCommentService {
    private final PostService postService;
    private final PostCommentRepository postCommentRepository;

    public PostCommentService(PostService postService, PostCommentRepository postCommentRepository) {
        this.postService = postService;
        this.postCommentRepository = postCommentRepository;
    }

    public List<PostComment> findAll(int postId) {
        Post post = postService.findById(postId);

        return postCommentRepository.findByPostOrderByIdDesc(post);
    }

    public PostComment findById(int postId, int id) {
        Post post = postService.findById(postId);

        return findByPostAndId(post, id);
    }

    @Transactional
    public PostComment write(int postId, String content) {
        Post post = postService.findById(postId);

        PostComment postComment = new PostComment();
        postComment.setPost(post);
        postComment.setContent(content);
        post.getComments().add(postComment);

        return postCommentRepository.save(postComment);
    }

    @Transactional
    public void delete(int postId, int id) {
        Post post = postService.findById(postId);
        PostComment postComment = findByPostAndId(post, id);

        post.getComments().remove(postComment);
        postCommentRepository.delete(postComment);
    }

    // 다른 글에 달린 댓글을 이 글의 댓글로 조회·삭제하지 못하도록 글과 함께 조회한다.
    private PostComment findByPostAndId(Post post, int id) {
        return postCommentRepository.findByPostAndId(post, id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "%d번 글에 %d번 댓글은 존재하지 않습니다.".formatted(post.getId(), id)
                ));
    }
}
