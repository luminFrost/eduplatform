package com.edu.eduplatform.course.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CourseResponseTest {

    @Test
    void isRecentlyAdded_7일_이내_생성이면_true() {
        CourseResponse response = response(LocalDateTime.now().minusDays(1));

        assertThat(response.isRecentlyAdded()).isTrue();
    }

    @Test
    void isRecentlyAdded_7일_초과면_false() {
        CourseResponse response = response(LocalDateTime.now().minusDays(8));

        assertThat(response.isRecentlyAdded()).isFalse();
    }

    @Test
    void isRecentlyAdded_createdAt이_없으면_false() {
        CourseResponse response = response(null);

        assertThat(response.isRecentlyAdded()).isFalse();
    }

    private static CourseResponse response(LocalDateTime createdAt) {
        return new CourseResponse(
                1L, "제목", "설명", "📘",
                MemberType.ADULT, EnglishLevel.BEGINNER, null,
                Set.of(), null, createdAt);
    }
}
