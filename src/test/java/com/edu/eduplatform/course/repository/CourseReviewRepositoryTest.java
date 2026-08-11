package com.edu.eduplatform.course.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.edu.eduplatform.course.domain.CourseReview;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class CourseReviewRepositoryTest {

    @Autowired
    private CourseReviewRepository courseReviewRepository;

    @Test
    void findByMemberIdAndCourseId_존재하면_반환한다() {
        courseReviewRepository.save(CourseReview.builder().memberId(1L).courseId(10L).rating(5).comment("좋아요").build());

        assertThat(courseReviewRepository.findByMemberIdAndCourseId(1L, 10L)).isPresent();
        assertThat(courseReviewRepository.findByMemberIdAndCourseId(1L, 99L)).isEmpty();
    }

    @Test
    void findByCourseIdOrderByIdDesc_최신순으로_반환한다() {
        courseReviewRepository.save(CourseReview.builder().memberId(1L).courseId(10L).rating(5).comment("첫번째").build());
        courseReviewRepository.save(CourseReview.builder().memberId(2L).courseId(10L).rating(3).comment("두번째").build());

        assertThat(courseReviewRepository.findByCourseIdOrderByIdDesc(10L))
                .extracting(CourseReview::getComment)
                .containsExactly("두번째", "첫번째");
    }

    @Test
    void deleteByMemberIdAndCourseId_삭제한다() {
        courseReviewRepository.save(CourseReview.builder().memberId(1L).courseId(10L).rating(5).comment("좋아요").build());

        courseReviewRepository.deleteByMemberIdAndCourseId(1L, 10L);

        assertThat(courseReviewRepository.findByMemberIdAndCourseId(1L, 10L)).isEmpty();
    }

    @Test
    void countByMemberId_회원이_작성한_리뷰_수를_센다() {
        courseReviewRepository.save(CourseReview.builder().memberId(1L).courseId(10L).rating(5).build());
        courseReviewRepository.save(CourseReview.builder().memberId(1L).courseId(20L).rating(4).build());
        courseReviewRepository.save(CourseReview.builder().memberId(2L).courseId(10L).rating(3).build());

        assertThat(courseReviewRepository.countByMemberId(1L)).isEqualTo(2);
        assertThat(courseReviewRepository.countByMemberId(99L)).isEqualTo(0);
    }

    @Test
    void 평균과_개수를_집계한다() {
        courseReviewRepository.save(CourseReview.builder().memberId(1L).courseId(10L).rating(5).build());
        courseReviewRepository.save(CourseReview.builder().memberId(2L).courseId(10L).rating(3).build());

        assertThat(courseReviewRepository.findAverageRatingByCourseId(10L)).isCloseTo(4.0, within(0.01));
        assertThat(courseReviewRepository.countByCourseId(10L)).isEqualTo(2);
    }

    @Test
    void findAllByOrderByIdDesc_전체_리뷰를_최신순으로_반환한다() {
        courseReviewRepository.save(CourseReview.builder().memberId(1L).courseId(10L).rating(5).comment("첫번째").build());
        courseReviewRepository.save(CourseReview.builder().memberId(2L).courseId(20L).rating(3).comment("두번째").build());

        assertThat(courseReviewRepository.findAllByOrderByIdDesc())
                .extracting(CourseReview::getComment)
                .containsExactly("두번째", "첫번째");
    }

    @Test
    void 여러_코스를_한번에_집계한다() {
        courseReviewRepository.save(CourseReview.builder().memberId(1L).courseId(10L).rating(5).build());
        courseReviewRepository.save(CourseReview.builder().memberId(2L).courseId(10L).rating(3).build());
        courseReviewRepository.save(CourseReview.builder().memberId(1L).courseId(20L).rating(2).build());

        List<CourseReviewRepository.CourseRatingProjection> summaries =
                courseReviewRepository.findRatingSummaries(List.of(10L, 20L));

        assertThat(summaries).hasSize(2);
        assertThat(summaries).anySatisfy(s -> {
            if (s.getCourseId().equals(10L)) {
                assertThat(s.getAverageRating()).isCloseTo(4.0, within(0.01));
                assertThat(s.getReviewCount()).isEqualTo(2);
            }
        });
    }
}
