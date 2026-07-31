package com.edu.eduplatform.course.dto;

/** 개인 코스 생성 결과. 동일한 focusAreas의 개인 코스가 이미 있으면 새로 만들지 않고 기존 코스를 반환한다({@code created=false}). */
public record PersonalCourseCreationResult(CourseResponse course, boolean created) {
}
