package com.edu.eduplatform.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.course.domain.CourseBookmark;
import java.util.List;
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

    @Test
    void countByMemberId_회원이_즐겨찾기한_코스_수를_센다() {
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(1L).courseId(10L).build());
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(1L).courseId(20L).build());
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(2L).courseId(10L).build());

        assertThat(courseBookmarkRepository.countByMemberId(1L)).isEqualTo(2);
        assertThat(courseBookmarkRepository.countByMemberId(99L)).isEqualTo(0);
    }

    @Test
    void countByCourseIdIn_여러_코스의_즐겨찾기_개수를_한번에_집계한다() {
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(1L).courseId(10L).build());
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(2L).courseId(10L).build());
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(1L).courseId(20L).build());

        List<CourseBookmarkRepository.CourseBookmarkCountProjection> counts =
                courseBookmarkRepository.countByCourseIdIn(List.of(10L, 20L));

        assertThat(counts).hasSize(2);
        assertThat(counts).anySatisfy(c -> {
            if (c.getCourseId().equals(10L)) {
                assertThat(c.getBookmarkCount()).isEqualTo(2L);
            } else {
                assertThat(c.getBookmarkCount()).isEqualTo(1L);
            }
        });
    }
}
