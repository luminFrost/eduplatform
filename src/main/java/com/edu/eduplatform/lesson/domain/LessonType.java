package com.edu.eduplatform.lesson.domain;

/**
 * 레슨의 주된 학습 활동 유형. PRODUCT.md 3-1의 종합형 학습 5대 영역과 동일하다.
 * 듣기(LISTENING)·말하기(SPEAKING)는 MVP 이후 오디오/음성인식 도입 시 콘텐츠가 채워진다.
 */
public enum LessonType {
    VOCAB,
    READING,
    WRITING,
    LISTENING,
    SPEAKING
}
