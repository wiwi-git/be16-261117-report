package com.be16261117report.post.post.service;

import com.be16261117report.post.post.entity.Post;
import com.be16261117report.post.post.repository.PostRepository;
import com.be16261117report.post.postComment.entity.PostComment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public long count() {
        return postRepository.count();
    }

    public List<Post> findAll() {
        return postRepository.findAllByOrderByIdDesc();
    }

    public Post findById(int id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "%d번 글은 존재하지 않습니다.".formatted(id)
                ));
    }

    @Transactional
    public Post write(String title, String content) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);

        return postRepository.save(post);
    }

    @Transactional
    public void delete(int id) {
        Post post = findById(id);
        postRepository.delete(post);
    }

    // 댓글 목록 (최신 댓글 먼저)
    public List<PostComment> findComments(Post post) {
        return post.getComments().reversed();
    }

    public PostComment findCommentById(Post post, int id) {
        return post.findCommentById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "%d번 글에 %d번 댓글은 존재하지 않습니다.".formatted(post.getId(), id)
                ));
    }

    // 글의 댓글 컬렉션에 추가만 하면 커밋 시점에 cascade(PERSIST)로 저장된다.
    // 다만 응답에 댓글 id가 필요하므로 flush로 INSERT를 바로 실행해 id를 받아 온다.
    @Transactional
    public PostComment writeComment(Post post, String content) {
        PostComment postComment = post.addComment(content);
        postRepository.flush();

        return postComment;
    }

    // 글의 댓글 컬렉션에서 빼기만 하면 커밋 시점에 orphanRemoval로 DELETE 된다.
    @Transactional
    public void deleteComment(Post post, PostComment postComment) {
        post.deleteComment(postComment);
    }
}
