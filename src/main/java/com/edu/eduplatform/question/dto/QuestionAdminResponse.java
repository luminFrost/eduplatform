package com.edu.eduplatform.question.dto;

import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import java.util.List;

public record QuestionAdminResponse(
        Long id,
        MemberType targetType,
        EnglishLevel level,
        LessonType lessonType,
        String prompt,
        String audioText,
        List<String> options,
        int correctOptionIndex
) {
}
