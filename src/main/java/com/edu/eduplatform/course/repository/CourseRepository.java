package com.edu.eduplatform.course.repository;

import com.edu.eduplatform.course.domain.Course;
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
            order by c.id asc
            """)
    List<Course> search(@Param("targetType") MemberType targetType, @Param("level") EnglishLevel level);

    List<Course> findByOwnerIdOrderByIdDesc(Long ownerId);
}
