package com.edu.eduplatform.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.course.domain.CourseReviewReport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class CourseReviewReportRepositoryTest {

    @Autowired
    private CourseReviewReportRepository courseReviewReportRepository;

    @Test
    void existsByMemberIdAndReviewId_존재하면_true를_반환한다() {
        courseReviewReportRepository.save(CourseReviewReport.builder().memberId(1L).reviewId(10L).build());

        assertThat(courseReviewReportRepository.existsByMemberIdAndReviewId(1L, 10L)).isTrue();
        assertThat(courseReviewReportRepository.existsByMemberIdAndReviewId(1L, 99L)).isFalse();
    }

    @Test
    void countByReviewIdIn_여러_리뷰의_신고수를_한번에_집계한다() {
        courseReviewReportRepository.save(CourseReviewReport.builder().memberId(1L).reviewId(10L).build());
        courseReviewReportRepository.save(CourseReviewReport.builder().memberId(2L).reviewId(10L).build());
        courseReviewReportRepository.save(CourseReviewReport.builder().memberId(1L).reviewId(20L).build());

        List<CourseReviewReportRepository.ReviewReportCountProjection> counts =
                courseReviewReportRepository.countByReviewIdIn(List.of(10L, 20L));

        assertThat(counts).hasSize(2);
        assertThat(counts).anySatisfy(c -> {
            if (c.getReviewId().equals(10L)) {
                assertThat(c.getReportCount()).isEqualTo(2L);
            } else {
                assertThat(c.getReportCount()).isEqualTo(1L);
            }
        });
    }
}
