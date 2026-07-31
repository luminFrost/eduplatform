package com.edu.eduplatform.course.dto;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.domain.CourseCriteriaSource;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import java.util.Set;

public record CourseResponse(
        Long id,
        String title,
        String description,
        String emoji,
        MemberType targetType,
        EnglishLevel level,
        Long ownerId,
        Set<LessonType> focusAreas,
        CourseCriteriaSource criteriaSource
) {

    public boolean isPersonal() {
        return ownerId != null;
    }

    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getEmoji(),
                course.getTargetType(),
                course.getLevel(),
                course.getOwnerId(),
                course.getFocusAreas(),
                course.getCriteriaSource()
        );
    }
}
