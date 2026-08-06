package com.edu.eduplatform.course.dto;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.domain.CourseCriteriaSource;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import java.time.LocalDateTime;
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
        CourseCriteriaSource criteriaSource,
        LocalDateTime createdAt
) {

    private static final int RECENTLY_ADDED_DAYS = 7;

    public boolean isPersonal() {
        return ownerId != null;
    }

    public boolean isRecentlyAdded() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(RECENTLY_ADDED_DAYS));
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
                course.getCriteriaSource(),
                course.getCreatedAt()
        );
    }
}
