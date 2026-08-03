package com.edu.eduplatform.course.repository;

import com.edu.eduplatform.course.domain.CourseBookmark;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseBookmarkRepository extends JpaRepository<CourseBookmark, Long> {

    boolean existsByMemberIdAndCourseId(Long memberId, Long courseId);

    List<CourseBookmark> findByMemberIdOrderByIdDesc(Long memberId);

    void deleteByMemberIdAndCourseId(Long memberId, Long courseId);
}
