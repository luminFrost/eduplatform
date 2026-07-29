package com.edu.eduplatform.lesson.dto;

public record LessonDetailResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String title,
        int orderNo,
        String content,
        int totalLessonsInCourse,
        Long prevLessonId,
        String prevLessonTitle,
        Long nextLessonId,
        String nextLessonTitle
) {
}
