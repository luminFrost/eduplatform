package com.edu.eduplatform.course.dto;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;

public record CourseResponse(
        Long id,
        String title,
        String description,
        String emoji,
        MemberType targetType,
        EnglishLevel level
) {

    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getEmoji(),
                course.getTargetType(),
                course.getLevel()
        );
    }
}
