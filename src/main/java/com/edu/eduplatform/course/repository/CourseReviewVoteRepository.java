package com.edu.eduplatform.course.repository;

import com.edu.eduplatform.course.domain.CourseReviewVote;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseReviewVoteRepository extends JpaRepository<CourseReviewVote, Long> {

    boolean existsByMemberIdAndReviewId(Long memberId, Long reviewId);

    void deleteByMemberIdAndReviewId(Long memberId, Long reviewId);

    /** 리뷰 목록 화면에서 리뷰마다 따로 집계 쿼리를 날리지 않도록 reviewId 여러 개를 한 번에 집계한다. */
    @Query("select v.reviewId as reviewId, count(v) as voteCount from CourseReviewVote v "
            + "where v.reviewId in :reviewIds group by v.reviewId")
    List<ReviewVoteCountProjection> countByReviewIdIn(@Param("reviewIds") Collection<Long> reviewIds);

    @Query("select v.reviewId from CourseReviewVote v where v.memberId = :memberId and v.reviewId in :reviewIds")
    List<Long> findVotedReviewIds(@Param("memberId") Long memberId, @Param("reviewIds") Collection<Long> reviewIds);

    interface ReviewVoteCountProjection {
        Long getReviewId();

        Long getVoteCount();
    }
}
