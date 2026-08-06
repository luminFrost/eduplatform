package com.edu.eduplatform.common.service;

import com.edu.eduplatform.common.dto.AdminStatsSummary;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.progress.domain.LearningProgress;
import com.edu.eduplatform.progress.dto.DailyActivityResponse;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 가입·학습 완료 추이 같은 운영 지표를 집계한다. Member와 LearningProgress 두 도메인을
 * 넘나드는 관리자 전용 집계라 {@link ContentCoverageService}와 같은 이유로 특정 도메인 서비스에
 * 얹지 않고 여기 둔다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private static final int TREND_DAYS = 14;
    private static final int RECENT_SIGNUP_DAYS = 7;

    private final MemberRepository memberRepository;
    private final LearningProgressRepository learningProgressRepository;

    public AdminStatsSummary getSummary() {
        List<Member> members = memberRepository.findAll();
        List<LearningProgress> progress = learningProgressRepository.findAll();

        LocalDate today = LocalDate.now();
        LocalDate signupCutoff = today.minusDays(RECENT_SIGNUP_DAYS - 1);
        long newMembersLast7Days = members.stream()
                .filter(m -> !m.getCreatedAt().toLocalDate().isBefore(signupCutoff))
                .count();
        long totalCompletions = progress.stream().filter(LearningProgress::isCompleted).count();

        Map<LocalDate, Long> signupsByDate = members.stream()
                .collect(Collectors.groupingBy(m -> m.getCreatedAt().toLocalDate(), Collectors.counting()));
        Map<LocalDate, Long> completionsByDate = progress.stream()
                .filter(LearningProgress::isCompleted)
                .collect(Collectors.groupingBy(p -> p.getCompletedAt().toLocalDate(), Collectors.counting()));

        List<DailyActivityResponse> dailySignups = buildTrend(today, signupsByDate);
        List<DailyActivityResponse> dailyCompletions = buildTrend(today, completionsByDate);

        return new AdminStatsSummary(
                members.size(), newMembersLast7Days, totalCompletions,
                dailySignups, maxCount(dailySignups),
                dailyCompletions, maxCount(dailyCompletions));
    }

    private static List<DailyActivityResponse> buildTrend(LocalDate today, Map<LocalDate, Long> countsByDate) {
        LocalDate start = today.minusDays(TREND_DAYS - 1);
        return start.datesUntil(today.plusDays(1))
                .map(d -> new DailyActivityResponse(d, dayLabel(d), countsByDate.getOrDefault(d, 0L).intValue()))
                .toList();
    }

    private static int maxCount(List<DailyActivityResponse> trend) {
        return trend.stream().mapToInt(DailyActivityResponse::completedCount).max().orElse(0);
    }

    private static String dayLabel(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}
