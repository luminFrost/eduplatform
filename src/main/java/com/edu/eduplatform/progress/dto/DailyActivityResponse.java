package com.edu.eduplatform.progress.dto;

import java.time.LocalDate;

public record DailyActivityResponse(LocalDate date, String dayLabel, int completedCount) {
}
