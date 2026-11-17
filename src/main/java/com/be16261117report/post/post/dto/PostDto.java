package com.be16261117report.post.post.dto;

import com.be16261117report.post.post.entity.Post;

import java.time.LocalDateTime;

public record PostDto(
        int id,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        String title,
        String content
) {
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getCreatedDate(),
                post.getModifiedDate(),
                post.getTitle(),
                post.getContent()
        );
    }
}
