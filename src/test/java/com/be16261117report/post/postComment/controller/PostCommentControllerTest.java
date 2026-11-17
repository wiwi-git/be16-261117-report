package com.be16261117report.post.postComment.controller;

import com.be16261117report.post.post.entity.Post;
import com.be16261117report.post.post.service.PostService;
import com.be16261117report.post.postComment.entity.PostComment;
import jakarta.persistence.EntityManager;
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

import static org.hamcrest.Matchers.endsWith;
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
    private EntityManager em;

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

    // 댓글 추가·삭제는 컬렉션 변경 → flush 시점에 DB 반영이므로,
    // DB 상태를 확인하기 전에 flush 해서 쌓인 변경을 내보낸다.
    private long countCommentsInDb() {
        em.flush();
        return em.createQuery("select count(c) from PostComment c", Long.class)
                .getSingleResult();
    }

    // flush 후 영속성 컨텍스트를 비워 1차 캐시가 아닌 DB에서 다시 조회한다.
    private boolean existsCommentInDb(int id) {
        em.flush();
        em.clear();
        return em.find(PostComment.class, id) != null;
    }

    @Test
    @DisplayName("댓글 다건 조회 - 최신 댓글이 먼저 나온다")
    void getItems() throws Exception {
        PostComment comment1 = postService.writeComment(post, "댓글1");
        PostComment comment2 = postService.writeComment(post, "댓글2");

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
        postService.writeComment(post, "댓글1");
        postService.writeComment(otherPost, "다른 글 댓글");

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
        PostComment comment = postService.writeComment(post, "댓글1");

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
        PostComment otherComment = postService.writeComment(otherPost, "다른 글 댓글");

        mvc.perform(get(url(post.getId(), otherComment.getId())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 작성 - 글의 댓글 컬렉션에 추가되고 DB에 저장된다")
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
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.statusCode").doesNotExist())
                .andExpect(jsonPath("$.msg").value(endsWith("번 댓글이 작성되었습니다.")))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.postId").value(post.getId()))
                .andExpect(jsonPath("$.data.content").value("새 댓글"));

        assertThat(post.getComments()).hasSize(1);
        assertThat(countCommentsInDb()).isEqualTo(1);
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

        assertThat(countCommentsInDb()).isZero();
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

        assertThat(countCommentsInDb()).isZero();
    }

    @Test
    @DisplayName("댓글 삭제 - 글의 댓글 컬렉션에서 빠지고 DB에서도 삭제된다(orphanRemoval)")
    void deleteItem() throws Exception {
        PostComment comment = postService.writeComment(post, "댓글1");

        mvc.perform(delete(url(post.getId(), comment.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.statusCode").doesNotExist())
                .andExpect(jsonPath("$.msg").value(
                        "%d번 글의 %d번 댓글이 삭제되었습니다.".formatted(post.getId(), comment.getId())
                ))
                .andExpect(jsonPath("$.data").isEmpty());

        assertThat(post.getComments()).isEmpty();
        assertThat(existsCommentInDb(comment.getId())).isFalse();
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
        PostComment otherComment = postService.writeComment(otherPost, "다른 글 댓글");

        mvc.perform(delete(url(post.getId(), otherComment.getId())))
                .andDo(print())
                .andExpect(status().isNotFound());

        assertThat(existsCommentInDb(otherComment.getId())).isTrue();
    }

    @Test
    @DisplayName("글을 삭제하면 그 글의 댓글도 함께 삭제된다")
    void deletePostCascadesComments() throws Exception {
        PostComment comment = postService.writeComment(post, "댓글1");

        mvc.perform(delete("/api/v1/posts/%d".formatted(post.getId())))
                .andDo(print())
                .andExpect(status().isOk());

        assertThat(existsCommentInDb(comment.getId())).isFalse();
    }
}
