package com.edu.eduplatform.course.repository;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("""
            select c from Course c
            where c.ownerId is null
              and (:targetType is null or c.targetType = :targetType)
              and (:level is null or c.level = :level)
              and (:lessonType is null or c.id in (
                  select l.courseId from Lesson l where l.lessonType = :lessonType
              ))
              and (:keyword is null or lower(c.title) like lower(concat('%', :keyword, '%'))
                   or lower(c.description) like lower(concat('%', :keyword, '%'))
                   or c.id in (
                       select l.courseId from Lesson l where lower(cast(l.content as string)) like lower(concat('%', :keyword, '%'))
                   ))
            order by c.id asc
            """)
    List<Course> search(
            @Param("targetType") MemberType targetType,
            @Param("level") EnglishLevel level,
            @Param("lessonType") LessonType lessonType,
            @Param("keyword") String keyword
    );

    List<Course> findByOwnerIdOrderByIdDesc(Long ownerId);

    long countByOwnerId(Long ownerId);
}
