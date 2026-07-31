package com.edu.eduplatform.progress.dto;

import jakarta.validation.constraints.NotNull;

public record ProgressCompleteRequest(

        @NotNull(message = "레슨 id가 필요합니다.")
        Long lessonId
) {
}
