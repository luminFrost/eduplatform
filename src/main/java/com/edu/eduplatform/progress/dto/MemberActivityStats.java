package com.edu.eduplatform.progress.dto;

public record MemberActivityStats(Long memberId, int completedLessons, int currentStreak) {
}
