package com.edu.eduplatform.common.service;

import com.edu.eduplatform.common.dto.Badge;
import com.edu.eduplatform.course.repository.CourseBookmarkRepository;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.course.repository.CourseReviewRepository;
import com.edu.eduplatform.progress.dto.DashboardSummaryResponse;
import com.edu.eduplatform.progress.dto.WeeklyGoalProgress;
import com.edu.eduplatform.progress.service.ProgressService;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 완료 레슨 수·코스 완주 수·스트릭·주간목표 달성·리뷰 작성·즐겨찾기·개인 코스 생성 등 Progress와
 * Course 두 도메인을 넘나드는 값으로 업적 배지를 계산한다. {@link ContentCoverageService}/
 * {@link AdminStatsService}와 같은 이유로 특정 도메인 서비스에 얹지 않고 여기 둔다. 새 추적
 * 테이블 없이 기존 데이터로 매 요청마다 즉석 계산한다(stateless) — 스트릭·월간 캘린더와 같은 원칙.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final ProgressService progressService;
    private final CourseReviewRepository courseReviewRepository;
    private final CourseBookmarkRepository courseBookmarkRepository;
    private final CourseRepository courseRepository;

    private record BadgeStats(int completedLessons, int completedCourses, int streak,
            boolean weeklyGoalMet, long reviewCount, long bookmarkCount, long personalCourseCount) {
    }

    private record BadgeDefinition(String id, String emoji, String title, String description,
            Predicate<BadgeStats> achieved) {
    }

    private static final List<BadgeDefinition> CATALOG = List.of(
            new BadgeDefinition("first_lesson", "🌱", "첫 발걸음", "레슨 1개를 완료했어요",
                    s -> s.completedLessons() >= 1),
            new BadgeDefinition("lessons_10", "📚", "열공 10", "레슨 10개를 완료했어요",
                    s -> s.completedLessons() >= 10),
            new BadgeDefinition("lessons_50", "🎓", "열공 50", "레슨 50개를 완료했어요",
                    s -> s.completedLessons() >= 50),
            new BadgeDefinition("lessons_100", "🏆", "열공 100", "레슨 100개를 완료했어요",
                    s -> s.completedLessons() >= 100),
            new BadgeDefinition("course_1", "🥇", "첫 코스 완주", "코스 1개를 끝까지 완료했어요",
                    s -> s.completedCourses() >= 1),
            new BadgeDefinition("course_5", "🏅", "코스 마스터", "코스 5개를 끝까지 완료했어요",
                    s -> s.completedCourses() >= 5),
            new BadgeDefinition("streak_3", "🔥", "3일 연속 학습", "3일 연속으로 학습했어요",
                    s -> s.streak() >= 3),
            new BadgeDefinition("streak_7", "🔥🔥", "일주일 연속 학습", "7일 연속으로 학습했어요",
                    s -> s.streak() >= 7),
            new BadgeDefinition("streak_30", "🔥🔥🔥", "한 달 연속 학습", "30일 연속으로 학습했어요",
                    s -> s.streak() >= 30),
            new BadgeDefinition("weekly_goal", "🎯", "이번 주 목표 달성", "이번 주 학습 목표를 달성했어요",
                    BadgeStats::weeklyGoalMet),
            new BadgeDefinition("first_review", "⭐", "첫 후기 작성", "코스에 후기를 처음 남겼어요",
                    s -> s.reviewCount() >= 1),
            new BadgeDefinition("bookmarks_3", "📌", "즐겨찾기 수집가", "코스 3개를 즐겨찾기했어요",
                    s -> s.bookmarkCount() >= 3),
            new BadgeDefinition("personal_course", "🧭", "나만의 코스", "개인 코스를 만들었어요",
                    s -> s.personalCourseCount() >= 1)
    );

    public List<Badge> getBadges(Long memberId) {
        BadgeStats stats = collectStats(memberId);
        return CATALOG.stream()
                .map(def -> new Badge(def.id(), def.emoji(), def.title(), def.description(),
                        def.achieved().test(stats)))
                .toList();
    }

    private BadgeStats collectStats(Long memberId) {
        DashboardSummaryResponse summary = progressService.getDashboardSummary(memberId);
        WeeklyGoalProgress weeklyGoal = progressService.getWeeklyGoalProgress(memberId);
        return new BadgeStats(
                summary.completedLessons(),
                progressService.getCompletedCourseCount(memberId),
                summary.currentStreak(),
                weeklyGoal.goal() > 0 && weeklyGoal.completed() >= weeklyGoal.goal(),
                courseReviewRepository.countByMemberId(memberId),
                courseBookmarkRepository.countByMemberId(memberId),
                courseRepository.countByOwnerId(memberId));
    }
}
