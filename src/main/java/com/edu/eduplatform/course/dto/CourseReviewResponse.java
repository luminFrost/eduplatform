package com.edu.eduplatform.course.dto;

import java.time.LocalDateTime;

public record CourseReviewResponse(
        Long id,
        Long memberId,
        String nickname,
        int rating,
        String comment,
        LocalDateTime createdAt,
        long helpfulCount,
        boolean votedByCurrentMember
) {
}
