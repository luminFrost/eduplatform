package com.edu.eduplatform.course.dto;

import jakarta.validation.constraints.NotNull;

public record HistoryBasedCourseCreateRequest(

        @NotNull(message = "회원 id가 필요합니다.")
        Long memberId
) {
}
