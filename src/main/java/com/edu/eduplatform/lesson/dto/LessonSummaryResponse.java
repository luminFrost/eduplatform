package com.edu.eduplatform.lesson.dto;

import com.edu.eduplatform.lesson.domain.LessonType;

public record LessonSummaryResponse(
        Long id,
        String title,
        int orderNo,
        LessonType lessonType,
        String iconImage
) {
}
