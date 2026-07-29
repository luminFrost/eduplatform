package com.edu.eduplatform.lesson.dto;

import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;

public record LessonSummaryResponse(
        Long id,
        String title,
        int orderNo,
        LessonType lessonType
) {

    public static LessonSummaryResponse from(Lesson lesson) {
        return new LessonSummaryResponse(lesson.getId(), lesson.getTitle(), lesson.getOrderNo(), lesson.getLessonType());
    }
}
