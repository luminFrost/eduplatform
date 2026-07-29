package com.edu.eduplatform.lesson.dto;

import java.util.List;

public record LessonDetailResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String title,
        int orderNo,
        List<ContentLine> contentLines,
        int totalLessonsInCourse,
        Long prevLessonId,
        String prevLessonTitle,
        Long nextLessonId,
        String nextLessonTitle
) {

    public enum LineType {
        /** 마스코트 인사/도입 문구. 주로 초등 코스에서 사용. */
        INTRO,
        /** 영어 — 한국어 쌍으로 구성된 예문. */
        PHRASE,
        /** 구분 없는 단문 한 줄 (예: 파닉스처럼 영단어가 문장에 섞인 경우). */
        NOTE
    }

    public record ContentLine(LineType type, String text, String subtext) {
    }
}
