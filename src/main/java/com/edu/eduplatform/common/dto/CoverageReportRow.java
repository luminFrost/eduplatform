package com.edu.eduplatform.common.dto;

import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import java.util.Map;

/** 대상·레벨 조합 하나의 영역별(어휘/읽기/쓰기/듣기/말하기) 레슨·문항 수. 관리자 커버리지 대시보드에 쓰인다. */
public record CoverageReportRow(
        MemberType targetType,
        EnglishLevel level,
        Map<LessonType, Integer> lessonCountByType,
        Map<LessonType, Integer> questionCountByType
) {
}
