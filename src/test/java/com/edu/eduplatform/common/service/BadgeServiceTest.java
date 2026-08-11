package com.edu.eduplatform.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.common.dto.Badge;
import com.edu.eduplatform.course.repository.CourseBookmarkRepository;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.course.repository.CourseReviewRepository;
import com.edu.eduplatform.progress.dto.DashboardSummaryResponse;
import com.edu.eduplatform.progress.dto.WeeklyGoalProgress;
import com.edu.eduplatform.progress.service.ProgressService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private ProgressService progressService;

    @Mock
    private CourseReviewRepository courseReviewRepository;

    @Mock
    private CourseBookmarkRepository courseBookmarkRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private BadgeService badgeService;

    @Test
    void getBadges_카탈로그_전체를_반환한다() {
        stubStats(0, 0, 0, new WeeklyGoalProgress(0, 0), 0, 0, 0);

        List<Badge> badges = badgeService.getBadges(1L);

        assertThat(badges).hasSize(13);
        assertThat(badges).allSatisfy(b -> assertThat(b.achieved()).isFalse());
    }

    @Test
    void getBadges_완료_레슨수_기준선에서_경계값을_정확히_판정한다() {
        stubStats(9, 0, 0, new WeeklyGoalProgress(0, 0), 0, 0, 0);
        assertThat(byId(badgeService.getBadges(1L), "lessons_10").achieved()).isFalse();

        stubStats(10, 0, 0, new WeeklyGoalProgress(0, 0), 0, 0, 0);
        assertThat(byId(badgeService.getBadges(1L), "lessons_10").achieved()).isTrue();
    }

    @Test
    void getBadges_코스_완주수_스트릭_기준을_판정한다() {
        stubStats(0, 5, 7, new WeeklyGoalProgress(0, 0), 0, 0, 0);

        List<Badge> badges = badgeService.getBadges(1L);

        assertThat(byId(badges, "course_1").achieved()).isTrue();
        assertThat(byId(badges, "course_5").achieved()).isTrue();
        assertThat(byId(badges, "streak_3").achieved()).isTrue();
        assertThat(byId(badges, "streak_7").achieved()).isTrue();
        assertThat(byId(badges, "streak_30").achieved()).isFalse();
    }

    @Test
    void getBadges_주간목표는_목표가_설정되고_달성했을_때만_획득한다() {
        stubStats(0, 0, 0, new WeeklyGoalProgress(0, 5), 0, 0, 0);
        assertThat(byId(badgeService.getBadges(1L), "weekly_goal").achieved()).isFalse();

        stubStats(0, 0, 0, new WeeklyGoalProgress(5, 3), 0, 0, 0);
        assertThat(byId(badgeService.getBadges(1L), "weekly_goal").achieved()).isFalse();

        stubStats(0, 0, 0, new WeeklyGoalProgress(5, 5), 0, 0, 0);
        assertThat(byId(badgeService.getBadges(1L), "weekly_goal").achieved()).isTrue();
    }

    @Test
    void getBadges_리뷰_즐겨찾기_개인코스_기준을_판정한다() {
        stubStats(0, 0, 0, new WeeklyGoalProgress(0, 0), 1, 3, 1);

        List<Badge> badges = badgeService.getBadges(1L);

        assertThat(byId(badges, "first_review").achieved()).isTrue();
        assertThat(byId(badges, "bookmarks_3").achieved()).isTrue();
        assertThat(byId(badges, "personal_course").achieved()).isTrue();
    }

    private void stubStats(int completedLessons, int completedCourses, int streak,
            WeeklyGoalProgress weeklyGoalProgress, long reviewCount, long bookmarkCount, long personalCourseCount) {
        when(progressService.getDashboardSummary(1L))
                .thenReturn(new DashboardSummaryResponse(completedLessons, 0, 0, streak));
        when(progressService.getCompletedCourseCount(1L)).thenReturn(completedCourses);
        when(progressService.getWeeklyGoalProgress(1L)).thenReturn(weeklyGoalProgress);
        when(courseReviewRepository.countByMemberId(1L)).thenReturn(reviewCount);
        when(courseBookmarkRepository.countByMemberId(1L)).thenReturn(bookmarkCount);
        when(courseRepository.countByOwnerId(1L)).thenReturn(personalCourseCount);
    }

    private static Badge byId(List<Badge> badges, String id) {
        Map<String, Badge> byId = badges.stream()
                .collect(java.util.stream.Collectors.toMap(Badge::id, b -> b));
        return byId.get(id);
    }
}
