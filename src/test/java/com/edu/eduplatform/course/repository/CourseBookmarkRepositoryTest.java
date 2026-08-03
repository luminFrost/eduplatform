package com.edu.eduplatform.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.course.domain.CourseBookmark;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class CourseBookmarkRepositoryTest {

    @Autowired
    private CourseBookmarkRepository courseBookmarkRepository;

    @Test
    void existsByMemberIdAndCourseId_존재하면_true를_반환한다() {
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(1L).courseId(10L).build());

        assertThat(courseBookmarkRepository.existsByMemberIdAndCourseId(1L, 10L)).isTrue();
        assertThat(courseBookmarkRepository.existsByMemberIdAndCourseId(1L, 99L)).isFalse();
    }

    @Test
    void findByMemberIdOrderByIdDesc_최신순으로_반환한다() {
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(1L).courseId(10L).build());
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(1L).courseId(20L).build());
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(2L).courseId(30L).build());

        assertThat(courseBookmarkRepository.findByMemberIdOrderByIdDesc(1L))
                .extracting(CourseBookmark::getCourseId)
                .containsExactly(20L, 10L);
    }

    @Test
    void deleteByMemberIdAndCourseId_삭제한다() {
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(1L).courseId(10L).build());

        courseBookmarkRepository.deleteByMemberIdAndCourseId(1L, 10L);

        assertThat(courseBookmarkRepository.existsByMemberIdAndCourseId(1L, 10L)).isFalse();
    }
}
