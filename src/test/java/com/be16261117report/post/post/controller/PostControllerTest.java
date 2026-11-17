package com.be16261117report.post.post.controller;

import com.be16261117report.post.post.entity.Post;
import com.be16261117report.post.post.repository.PostRepository;
import com.be16261117report.post.post.service.PostService;
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
class PostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("글 다건 조회 - 최신 글이 먼저 나온다")
    void getItems() throws Exception {
        Post post1 = postService.write("제목1", "내용1");
        Post post2 = postService.write("제목2", "내용2");

        mvc.perform(get("/api/v1/posts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(post2.getId()))
                .andExpect(jsonPath("$[0].title").value("제목2"))
                .andExpect(jsonPath("$[1].id").value(post1.getId()));
    }

    @Test
    @DisplayName("글 단건 조회")
    void getItem() throws Exception {
        Post post = postService.write("제목1", "내용1");

        mvc.perform(get("/api/v1/posts/%d".formatted(post.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.title").value("제목1"))
                .andExpect(jsonPath("$.content").value("내용1"))
                .andExpect(jsonPath("$.createdDate").exists())
                .andExpect(jsonPath("$.modifiedDate").exists());
    }

    @Test
    @DisplayName("글 단건 조회 - 없는 글이면 404")
    void getItemNotFound() throws Exception {
        mvc.perform(get("/api/v1/posts/%d".formatted(Integer.MAX_VALUE)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("글 작성")
    void writeItem() throws Exception {
        mvc.perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "새 제목",
                                            "content": "새 내용"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.statusCode").doesNotExist())
                .andExpect(jsonPath("$.msg").value(endsWith("번 글이 작성되었습니다.")))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("새 제목"))
                .andExpect(jsonPath("$.data.content").value("새 내용"));

        assertThat(postRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("글 작성 - 제목이 비어 있으면 400")
    void writeItemWithBlankTitle() throws Exception {
        mvc.perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "",
                                            "content": "새 내용"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        assertThat(postRepository.count()).isZero();
    }

    @Test
    @DisplayName("글 삭제")
    void deleteItem() throws Exception {
        Post post = postService.write("제목1", "내용1");

        mvc.perform(delete("/api/v1/posts/%d".formatted(post.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.statusCode").doesNotExist())
                .andExpect(jsonPath("$.msg").value("%d번 글이 삭제되었습니다.".formatted(post.getId())))
                .andExpect(jsonPath("$.data").isEmpty());

        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    @Test
    @DisplayName("글 삭제 - 없는 글이면 404")
    void deleteItemNotFound() throws Exception {
        mvc.perform(delete("/api/v1/posts/%d".formatted(Integer.MAX_VALUE)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
