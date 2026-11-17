package com.be16261117report.post.postComment.repository;

import com.be16261117report.post.post.entity.Post;
import com.be16261117report.post.postComment.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {
    List<PostComment> findByPostOrderByIdDesc(Post post);

    Optional<PostComment> findByPostAndId(Post post, int id);
}
