package com.edu.eduplatform.question.dto;

import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.question.domain.Question;
import java.util.List;

/** correctOptionIndex는 의도적으로 뺀다 — 정답 유출 방지. */
public record QuestionResponse(Long id, LessonType lessonType, String prompt, String audioText, List<String> options) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getLessonType(),
                question.getPrompt(),
                question.getAudioText(),
                question.getOptions()
        );
    }
}
