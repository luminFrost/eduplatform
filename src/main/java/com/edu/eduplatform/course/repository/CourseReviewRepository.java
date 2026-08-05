package com.edu.eduplatform.course.repository;

import com.edu.eduplatform.course.domain.CourseReview;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    Optional<CourseReview> findByMemberIdAndCourseId(Long memberId, Long courseId);

    List<CourseReview> findByCourseIdOrderByIdDesc(Long courseId);

    void deleteByMemberIdAndCourseId(Long memberId, Long courseId);

    long countByCourseId(Long courseId);

    @Query("select avg(r.rating) from CourseReview r where r.courseId = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);

    /** 코스 목록 화면에서 카드마다 따로 집계 쿼리를 날리지 않도록 courseId 여러 개를 한 번에 집계한다. */
    @Query("select r.courseId as courseId, avg(r.rating) as averageRating, count(r) as reviewCount "
            + "from CourseReview r where r.courseId in :courseIds group by r.courseId")
    List<CourseRatingProjection> findRatingSummaries(@Param("courseIds") Collection<Long> courseIds);

    interface CourseRatingProjection {
        Long getCourseId();

        Double getAverageRating();

        Long getReviewCount();
    }
}
