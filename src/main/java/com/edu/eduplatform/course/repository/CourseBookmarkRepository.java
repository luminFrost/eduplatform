package com.edu.eduplatform.course.repository;

import com.edu.eduplatform.course.domain.CourseBookmark;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseBookmarkRepository extends JpaRepository<CourseBookmark, Long> {

    boolean existsByMemberIdAndCourseId(Long memberId, Long courseId);

    List<CourseBookmark> findByMemberIdOrderByIdDesc(Long memberId);

    void deleteByMemberIdAndCourseId(Long memberId, Long courseId);

    /** 코스 목록 화면에서 카드마다 따로 집계 쿼리를 날리지 않도록 courseId 여러 개를 한 번에 집계한다. */
    @Query("select cb.courseId as courseId, count(cb) as bookmarkCount from CourseBookmark cb "
            + "where cb.courseId in :courseIds group by cb.courseId")
    List<CourseBookmarkCountProjection> countByCourseIdIn(@Param("courseIds") Collection<Long> courseIds);

    interface CourseBookmarkCountProjection {
        Long getCourseId();

        Long getBookmarkCount();
    }
}
