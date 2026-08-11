package com.edu.eduplatform.common.dto;

public record LeaderboardEntry(int rank, String nickname, int completedLessons,
        int currentStreak, boolean currentMember) {
}
