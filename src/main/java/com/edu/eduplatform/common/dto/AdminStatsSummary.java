package com.edu.eduplatform.common.dto;

import com.edu.eduplatform.progress.dto.DailyActivityResponse;
import java.util.List;

public record AdminStatsSummary(
        long totalMembers,
        long newMembersLast7Days,
        long totalCompletions,
        List<DailyActivityResponse> dailySignups,
        int maxDailySignup,
        List<DailyActivityResponse> dailyCompletions,
        int maxDailyCompletion
) {
}
