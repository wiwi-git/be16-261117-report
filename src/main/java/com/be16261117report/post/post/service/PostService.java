package com.be16261117report.post.post.service;

import com.be16261117report.post.post.entity.Post;
import com.be16261117report.post.post.repository.PostRepository;
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
}
