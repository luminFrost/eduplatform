package com.edu.eduplatform.progress.dto;

import java.time.LocalDateTime;

public record ReviewLessonResponse(
        Long lessonId,
        String lessonTitle,
        Long courseId,
        String courseTitle,
        LocalDateTime completedAt
) {
}
