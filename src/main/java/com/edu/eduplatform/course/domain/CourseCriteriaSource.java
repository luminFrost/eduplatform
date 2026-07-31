package com.edu.eduplatform.course.domain;

/**
 * 개인 코스의 비중 기준을 정한 방식. PRODUCT.md 3-2 참고.
 * 구현 우선순위: SELF_SELECTED → HISTORY_BASED → DIAGNOSTIC_TEST(최후순위).
 * 공식 코스(ownerId == null)는 이 값이 없다.
 */
public enum CourseCriteriaSource {
    SELF_SELECTED,
    HISTORY_BASED,
    DIAGNOSTIC_TEST
}
