package com.be16261117report.post.post.entity;

import com.be16261117report.global.entity.BaseEntity;
import com.be16261117report.post.postComment.entity.PostComment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Post extends BaseEntity {
    private String title;
    private String content;

    public Post(){}

    // 댓글은 글을 통해서만 추가·삭제한다.
    // PERSIST: 컬렉션에 추가된 댓글이 flush/커밋 시점에 함께 저장된다.
    // orphanRemoval: 컬렉션에서 빠진 댓글이 flush/커밋 시점에 삭제된다.
    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PostComment>  comments = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<PostComment> getComments() {
        return comments;
    }

    public void setComments(List<PostComment> comments) {
        this.comments = comments;
    }

    public PostComment addComment(String content) {
        PostComment postComment = new PostComment();
        postComment.setPost(this);
        postComment.setContent(content);

        comments.add(postComment);

        return postComment;
    }

    // 이 글에 달린 댓글 중에서만 찾으므로 다른 글의 댓글은 찾을 수 없다.
    public Optional<PostComment> findCommentById(int id) {
        return comments.stream()
                .filter(comment -> comment.getId() == id)
                .findFirst();
    }

    public boolean deleteComment(PostComment postComment) {
        return comments.remove(postComment);
    }
}
