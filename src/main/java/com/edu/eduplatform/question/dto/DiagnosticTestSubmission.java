package com.edu.eduplatform.question.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DiagnosticTestSubmission(

        @NotEmpty(message = "답안이 필요합니다.")
        List<@Valid QuestionAnswer> answers
) {
}
