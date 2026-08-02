package com.edu.eduplatform.lesson.dto;

public record LessonSearchResultResponse(
        Long lessonId,
        String lessonTitle,
        Long courseId,
        String courseTitle,
        String snippetText,
        String snippetSubtext
) {
}
