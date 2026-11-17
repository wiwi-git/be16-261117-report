package com.be16261117report.post.postComment.entity;

import com.be16261117report.global.entity.BaseEntity;
import com.be16261117report.post.post.entity.Post;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class PostComment extends BaseEntity {
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
