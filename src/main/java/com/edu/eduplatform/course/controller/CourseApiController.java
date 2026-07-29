package com.edu.eduplatform.course.controller;

import com.edu.eduplatform.course.dto.CourseCreateRequest;
import com.edu.eduplatform.course.dto.CourseResponse;
import com.edu.eduplatform.course.dto.PersonalCourseCreateRequest;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.service.CourseService;
import com.edu.eduplatform.lesson.dto.LessonSummaryResponse;
import com.edu.eduplatform.lesson.service.LessonService;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseApiController {

    private final CourseService courseService;
    private final LessonService lessonService;

    @GetMapping
    public List<CourseResponse> list(
            @RequestParam(required = false) MemberType target,
            @RequestParam(required = false) EnglishLevel level
    ) {
        return courseService.list(target, level);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseCreateRequest request) {
        return courseService.create(request);
    }

    @GetMapping("/{id}/lessons")
    public List<LessonSummaryResponse> lessons(@PathVariable Long id) {
        courseService.getCourse(id);
        return lessonService.listByCourse(id);
    }

    @PostMapping("/personal")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createPersonal(@Valid @RequestBody PersonalCourseCreateRequest request) {
        return courseService.createPersonalCourse(request);
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<String> handleCourseNotFound(CourseNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<String> handleMemberNotFound(MemberNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
