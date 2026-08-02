package com.edu.eduplatform.lesson.dto;

import com.edu.eduplatform.lesson.domain.LessonType;

/** 관리자 수정 폼에 쓰는, 원본 콘텐츠 문자열을 그대로 담은 응답. {@link LessonDetailResponse}는 파싱된 카드 형태라 편집에 안 맞는다. */
public record LessonAdminResponse(Long id, Long courseId, String title, int orderNo, String content, LessonType lessonType) {
}
