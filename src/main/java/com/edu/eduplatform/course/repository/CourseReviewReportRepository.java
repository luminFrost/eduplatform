package com.edu.eduplatform.course.repository;

import com.edu.eduplatform.course.domain.CourseReviewReport;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseReviewReportRepository extends JpaRepository<CourseReviewReport, Long> {

    boolean existsByMemberIdAndReviewId(Long memberId, Long reviewId);

    /** 관리자 리뷰 목록에서 리뷰마다 따로 집계 쿼리를 날리지 않도록 reviewId 여러 개를 한 번에 집계한다. */
    @Query("select r.reviewId as reviewId, count(r) as reportCount from CourseReviewReport r "
            + "where r.reviewId in :reviewIds group by r.reviewId")
    List<ReviewReportCountProjection> countByReviewIdIn(@Param("reviewIds") Collection<Long> reviewIds);

    interface ReviewReportCountProjection {
        Long getReviewId();

        Long getReportCount();
    }
}
