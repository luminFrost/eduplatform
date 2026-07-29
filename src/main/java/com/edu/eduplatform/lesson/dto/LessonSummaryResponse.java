package com.edu.eduplatform.lesson.dto;

import com.edu.eduplatform.lesson.domain.Lesson;

public record LessonSummaryResponse(
        Long id,
        String title,
        int orderNo
) {

    public static LessonSummaryResponse from(Lesson lesson) {
        return new LessonSummaryResponse(lesson.getId(), lesson.getTitle(), lesson.getOrderNo());
    }
}
