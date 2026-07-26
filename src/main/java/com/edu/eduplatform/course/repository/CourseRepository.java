package com.edu.eduplatform.course.repository;

import com.edu.eduplatform.course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
