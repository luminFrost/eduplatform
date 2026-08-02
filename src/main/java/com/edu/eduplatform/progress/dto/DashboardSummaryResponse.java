package com.edu.eduplatform.progress.dto;

public record DashboardSummaryResponse(int completedLessons, int coursesInProgress, int overallPercentage, int currentStreak) {
}
