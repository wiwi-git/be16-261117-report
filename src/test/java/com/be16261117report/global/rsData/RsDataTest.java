package com.be16261117report.global.rsData;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RsDataTest {
    @Test
    @DisplayName("resultCode의 '-' 앞부분을 statusCode로 파싱한다")
    void parseStatusCode() {
        assertThat(new RsData<>("201-1", "작성").statusCode()).isEqualTo(201);
        assertThat(new RsData<>("200-1", "삭제").statusCode()).isEqualTo(200);
        assertThat(new RsData<>("404-2", "없음").statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("data 없이 만들면 data는 null이다")
    void withoutData() {
        RsData<Void> rsData = new RsData<>("200-1", "삭제되었습니다.");

        assertThat(rsData.resultCode()).isEqualTo("200-1");
        assertThat(rsData.msg()).isEqualTo("삭제되었습니다.");
        assertThat(rsData.data()).isNull();
    }
}
