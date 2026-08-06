package com.edu.eduplatform.progress.dto;

public record WeeklyGoalProgress(int goal, int completed) {

    /** 목표보다 더 완료할 수 있어(예: 목표를 낮춘 뒤에도 이전 완료가 남아있는 경우) 100%로 캡한다. */
    public int percentage() {
        if (goal == 0) {
            return 0;
        }
        return Math.min(100, (int) Math.round(completed * 100.0 / goal));
    }
}
