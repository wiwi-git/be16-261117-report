package com.be16261117report.global.initData;

import com.be16261117report.post.post.entity.Post;
import com.be16261117report.post.post.service.PostService;
import com.be16261117report.post.postComment.service.PostCommentService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

// 글이 하나도 없을 때만 기본 데이터를 넣는다.
// test 프로필에서는 테스트가 직접 데이터를 만들고 개수를 검증하므로 실행하지 않는다.
@Configuration
@Profile("!test")
public class BaseInitData {
    // 같은 클래스 안에서 this.work()로 부르면 프록시를 거치지 않아 @Transactional이 무시된다.
    // 자기 자신의 프록시를 주입받아 self.work()로 호출해야 트랜잭션이 적용된다.
    // 생성 시점에는 아직 자기 자신이 만들어지지 않았으므로 @Lazy로 순환 참조를 피한다.
    private final BaseInitData self;
    private final PostService postService;
    private final PostCommentService postCommentService;

    public BaseInitData(
            @Lazy BaseInitData self,
            PostService postService,
            PostCommentService postCommentService
    ) {
        this.self = self;
        this.postService = postService;
        this.postCommentService = postCommentService;
    }

    @Bean
    public ApplicationRunner baseInitDataApplicationRunner() {
        return args -> self.work();
    }

    // 글·댓글 저장을 하나의 트랜잭션으로 묶어, 중간에 실패하면 전부 롤백된다.
    // (일부만 저장된 채 남으면 다음 실행 때 count() > 0 이라 다시 채워지지 않기 때문)
    // 프록시가 가로챌 수 있도록 public 이어야 한다.
    @Transactional
    public void work() {
        if (postService.count() > 0) return;

        Post post1 = postService.write("첫 번째 글", "첫 번째 글의 내용입니다.");
        Post post2 = postService.write("두 번째 글", "두 번째 글의 내용입니다.");
        Post post3 = postService.write("세 번째 글", "세 번째 글의 내용입니다.");

        // 댓글 5개를 글마다 2개, 2개, 1개로 나눠 단다.
        postCommentService.write(post1.getId(), "첫 번째 글의 첫 번째 댓글입니다.");
        postCommentService.write(post1.getId(), "첫 번째 글의 두 번째 댓글입니다.");

        postCommentService.write(post2.getId(), "두 번째 글의 첫 번째 댓글입니다.");
        postCommentService.write(post2.getId(), "두 번째 글의 두 번째 댓글입니다.");

        postCommentService.write(post3.getId(), "세 번째 글의 첫 번째 댓글입니다.");
    }
}
