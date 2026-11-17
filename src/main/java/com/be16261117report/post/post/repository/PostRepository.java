package com.be16261117report.post.post.repository;

import com.be16261117report.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findAllByOrderByIdDesc();
}
