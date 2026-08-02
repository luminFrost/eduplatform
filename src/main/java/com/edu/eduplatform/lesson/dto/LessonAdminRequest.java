package com.edu.eduplatform.lesson.dto;

import com.edu.eduplatform.lesson.domain.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LessonAdminRequest(

        @NotBlank(message = "레슨 제목을 입력해 주세요.")
        String title,

        int orderNo,

        @NotBlank(message = "레슨 내용을 입력해 주세요.")
        String content,

        @NotNull(message = "영역을 선택해 주세요.")
        LessonType lessonType
) {
}
