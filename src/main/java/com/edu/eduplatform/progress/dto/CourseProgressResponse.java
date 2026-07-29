package com.edu.eduplatform.progress.dto;

public record CourseProgressResponse(
        Long courseId,
        String courseTitle,
        String emoji,
        int totalLessons,
        int completedLessons
) {

    public int percentage() {
        return totalLessons == 0 ? 0 : (int) Math.round(completedLessons * 100.0 / totalLessons);
    }
}
