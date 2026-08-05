package com.edu.eduplatform.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.course.domain.CourseReviewVote;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class CourseReviewVoteRepositoryTest {

    @Autowired
    private CourseReviewVoteRepository courseReviewVoteRepository;

    @Test
    void existsByMemberIdAndReviewId_존재하면_true를_반환한다() {
        courseReviewVoteRepository.save(CourseReviewVote.builder().memberId(1L).reviewId(10L).build());

        assertThat(courseReviewVoteRepository.existsByMemberIdAndReviewId(1L, 10L)).isTrue();
        assertThat(courseReviewVoteRepository.existsByMemberIdAndReviewId(1L, 99L)).isFalse();
    }

    @Test
    void deleteByMemberIdAndReviewId_삭제한다() {
        courseReviewVoteRepository.save(CourseReviewVote.builder().memberId(1L).reviewId(10L).build());

        courseReviewVoteRepository.deleteByMemberIdAndReviewId(1L, 10L);

        assertThat(courseReviewVoteRepository.existsByMemberIdAndReviewId(1L, 10L)).isFalse();
    }

    @Test
    void countByReviewIdIn_여러_리뷰의_투표수를_한번에_집계한다() {
        courseReviewVoteRepository.save(CourseReviewVote.builder().memberId(1L).reviewId(10L).build());
        courseReviewVoteRepository.save(CourseReviewVote.builder().memberId(2L).reviewId(10L).build());
        courseReviewVoteRepository.save(CourseReviewVote.builder().memberId(1L).reviewId(20L).build());

        List<CourseReviewVoteRepository.ReviewVoteCountProjection> counts =
                courseReviewVoteRepository.countByReviewIdIn(List.of(10L, 20L));

        assertThat(counts).hasSize(2);
        assertThat(counts).anySatisfy(c -> {
            if (c.getReviewId().equals(10L)) {
                assertThat(c.getVoteCount()).isEqualTo(2L);
            } else {
                assertThat(c.getVoteCount()).isEqualTo(1L);
            }
        });
    }

    @Test
    void findVotedReviewIds_해당_회원이_투표한_리뷰만_반환한다() {
        courseReviewVoteRepository.save(CourseReviewVote.builder().memberId(1L).reviewId(10L).build());
        courseReviewVoteRepository.save(CourseReviewVote.builder().memberId(2L).reviewId(20L).build());

        List<Long> voted = courseReviewVoteRepository.findVotedReviewIds(1L, List.of(10L, 20L));

        assertThat(voted).containsExactly(10L);
    }
}
