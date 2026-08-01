package com.edu.eduplatform.question.dto;

import jakarta.validation.constraints.NotNull;

public record QuestionAnswer(

        @NotNull(message = "문항 id가 필요합니다.")
        Long questionId,

        @NotNull(message = "선택한 보기가 필요합니다.")
        Integer selectedOptionIndex
) {
}
