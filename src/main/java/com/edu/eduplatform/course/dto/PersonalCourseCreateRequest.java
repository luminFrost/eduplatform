package com.edu.eduplatform.course.dto;

import com.edu.eduplatform.lesson.domain.LessonType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record PersonalCourseCreateRequest(

        @NotNull(message = "회원 id가 필요합니다.")
        Long memberId,

        @NotEmpty(message = "비중을 둘 영역을 하나 이상 선택해 주세요.")
        Set<LessonType> focusAreas
) {
}
