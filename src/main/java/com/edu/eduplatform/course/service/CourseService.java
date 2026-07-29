package com.edu.eduplatform.course.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.dto.CourseCreateRequest;
import com.edu.eduplatform.course.dto.CourseResponse;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    public List<CourseResponse> list(MemberType targetType, EnglishLevel level) {
        return courseRepository.search(targetType, level).stream()
                .map(CourseResponse::from)
                .toList();
    }

    public CourseResponse getCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));

        return CourseResponse.from(course);
    }

    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .targetType(request.targetType())
                .level(request.level())
                .build();

        return CourseResponse.from(courseRepository.save(course));
    }
}
