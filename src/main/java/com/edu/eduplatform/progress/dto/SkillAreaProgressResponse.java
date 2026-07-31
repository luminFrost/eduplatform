package com.edu.eduplatform.progress.dto;

import com.edu.eduplatform.lesson.domain.LessonType;

public record SkillAreaProgressResponse(LessonType lessonType, int completedLessons, int totalLessons) {

    /**
     * 개인 코스는 레슨을 복사해서 만들어(같은 lessonType, 다른 id) 완료 수가 공식 커리큘럼 분모보다
     * 커질 수 있다 — 화면에 직접 찍는 값이라 100%로 캡한다.
     */
    public int percentage() {
        if (totalLessons == 0) {
            return 0;
        }
        return Math.min(100, (int) Math.round(completedLessons * 100.0 / totalLessons));
    }
}
