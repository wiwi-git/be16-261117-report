package com.be16261117report.post.postComment.dto;

import com.be16261117report.post.postComment.entity.PostComment;

import java.time.LocalDateTime;

public record PostCommentDto(
        int id,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        int postId,
        String content
) {
    public PostCommentDto(PostComment postComment) {
        this(
                postComment.getId(),
                postComment.getCreatedDate(),
                postComment.getModifiedDate(),
                postComment.getPost().getId(),
                postComment.getContent()
        );
    }
}
