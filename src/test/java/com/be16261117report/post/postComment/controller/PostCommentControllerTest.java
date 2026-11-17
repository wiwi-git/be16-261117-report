package com.be16261117report.post.postComment.controller;

import com.be16261117report.post.post.entity.Post;
import com.be16261117report.post.post.service.PostService;
import com.be16261117report.post.postComment.entity.PostComment;
import com.be16261117report.post.postComment.repository.PostCommentRepository;
import com.be16261117report.post.postComment.service.PostCommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PostCommentControllerTest {
    private static final int NOT_EXIST_ID = Integer.MAX_VALUE;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private PostCommentRepository postCommentRepository;

    private Post post;

    @BeforeEach
    void setUp() {
        post = postService.write("제목1", "내용1");
    }

    private String url(int postId) {
        return "/api/v1/posts/%d/comments".formatted(postId);
    }

    private String url(int postId, int commentId) {
        return "/api/v1/posts/%d/comments/%d".formatted(postId, commentId);
    }

    @Test
    @DisplayName("댓글 다건 조회 - 최신 댓글이 먼저 나온다")
    void getItems() throws Exception {
        PostComment comment1 = postCommentService.write(post.getId(), "댓글1");
        PostComment comment2 = postCommentService.write(post.getId(), "댓글2");

        mvc.perform(get(url(post.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(comment2.getId()))
                .andExpect(jsonPath("$[0].postId").value(post.getId()))
                .andExpect(jsonPath("$[0].content").value("댓글2"))
                .andExpect(jsonPath("$[1].id").value(comment1.getId()));
    }

    @Test
    @DisplayName("댓글 다건 조회 - 다른 글의 댓글은 나오지 않는다")
    void getItemsOnlyThisPost() throws Exception {
        Post otherPost = postService.write("제목2", "내용2");
        postCommentService.write(post.getId(), "댓글1");
        postCommentService.write(otherPost.getId(), "다른 글 댓글");

        mvc.perform(get(url(post.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("댓글1"));
    }

    @Test
    @DisplayName("댓글 다건 조회 - 없는 글이면 404")
    void getItemsPostNotFound() throws Exception {
        mvc.perform(get(url(NOT_EXIST_ID)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 단건 조회")
    void getItem() throws Exception {
        PostComment comment = postCommentService.write(post.getId(), "댓글1");

        mvc.perform(get(url(post.getId(), comment.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.content").value("댓글1"))
                .andExpect(jsonPath("$.createdDate").exists())
                .andExpect(jsonPath("$.modifiedDate").exists());
    }

    @Test
    @DisplayName("댓글 단건 조회 - 없는 댓글이면 404")
    void getItemNotFound() throws Exception {
        mvc.perform(get(url(post.getId(), NOT_EXIST_ID)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 단건 조회 - 다른 글의 댓글 번호로 조회하면 404")
    void getItemOfOtherPost() throws Exception {
        Post otherPost = postService.write("제목2", "내용2");
        PostComment otherComment = postCommentService.write(otherPost.getId(), "다른 글 댓글");

        mvc.perform(get(url(post.getId(), otherComment.getId())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 작성")
    void writeItem() throws Exception {
        mvc.perform(
                        post(url(post.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 댓글"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.content").value("새 댓글"));

        assertThat(postCommentRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글 작성 - 내용이 비어 있으면 400")
    void writeItemWithBlankContent() throws Exception {
        mvc.perform(
                        post(url(post.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": ""
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        assertThat(postCommentRepository.count()).isZero();
    }

    @Test
    @DisplayName("댓글 작성 - 없는 글이면 404")
    void writeItemPostNotFound() throws Exception {
        mvc.perform(
                        post(url(NOT_EXIST_ID))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 댓글"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        assertThat(postCommentRepository.count()).isZero();
    }

    @Test
    @DisplayName("댓글 삭제")
    void deleteItem() throws Exception {
        PostComment comment = postCommentService.write(post.getId(), "댓글1");

        mvc.perform(delete(url(post.getId(), comment.getId())))
                .andDo(print())
                .andExpect(status().isNoContent());

        assertThat(postCommentRepository.findById(comment.getId())).isEmpty();
    }

    @Test
    @DisplayName("댓글 삭제 - 없는 댓글이면 404")
    void deleteItemNotFound() throws Exception {
        mvc.perform(delete(url(post.getId(), NOT_EXIST_ID)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 삭제 - 다른 글의 댓글 번호로 삭제하면 404, 댓글은 남아 있다")
    void deleteItemOfOtherPost() throws Exception {
        Post otherPost = postService.write("제목2", "내용2");
        PostComment otherComment = postCommentService.write(otherPost.getId(), "다른 글 댓글");

        mvc.perform(delete(url(post.getId(), otherComment.getId())))
                .andDo(print())
                .andExpect(status().isNotFound());

        assertThat(postCommentRepository.findById(otherComment.getId())).isPresent();
    }

    @Test
    @DisplayName("글을 삭제하면 그 글의 댓글도 함께 삭제된다")
    void deletePostCascadesComments() throws Exception {
        PostComment comment = postCommentService.write(post.getId(), "댓글1");

        mvc.perform(delete("/api/v1/posts/%d".formatted(post.getId())))
                .andDo(print())
                .andExpect(status().isNoContent());

        assertThat(postCommentRepository.findById(comment.getId())).isEmpty();
    }
}
