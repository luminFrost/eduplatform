package com.edu.eduplatform.course.dto;

import java.time.LocalDateTime;

public record CourseReviewAdminResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String reviewerNickname,
        int rating,
        String comment,
        LocalDateTime createdAt,
        long reportCount
) {
}
