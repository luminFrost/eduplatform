package com.edu.eduplatform.progress.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.service.MemberService;
import com.edu.eduplatform.progress.domain.LearningProgress;
import com.edu.eduplatform.progress.dto.CourseProgressResponse;
import com.edu.eduplatform.progress.dto.DailyActivityResponse;
import com.edu.eduplatform.progress.dto.DashboardSummaryResponse;
import com.edu.eduplatform.progress.dto.ReviewLessonResponse;
import com.edu.eduplatform.progress.dto.SkillAreaProgressResponse;
import com.edu.eduplatform.progress.exception.InsufficientHistoryException;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressService {

    /** 이보다 적게 완료한 상태에서는 영역별 커버리지 편차가 우연에 가까워 추천 근거로 삼지 않는다. */
    private static final int MIN_HISTORY_LESSONS = 3;

    /** 완료한 지 이만큼 지나면 복습 대상으로 본다. */
    private static final int REVIEW_DUE_AFTER_DAYS = 3;
    /** 복습 목록에 한 번에 보여줄 최대 개수. */
    private static final int REVIEW_LIST_LIMIT = 10;

    /** "오늘 학습 안 해도 어제까지 했으면 아직 안 끊긴 것"으로 보는 유예 기준(일). */
    private static final int STREAK_GRACE_PERIOD_DAYS = 1;

    private final LearningProgressRepository learningProgressRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final MemberService memberService;

    public boolean isCompleted(Long memberId, Long lessonId) {
        return learningProgressRepository.findByMemberIdAndLessonId(memberId, lessonId)
                .map(LearningProgress::isCompleted)
                .orElse(false);
    }

    /** 코스의 레슨을 전부 완료했는지 확인한다. 레슨이 하나도 없는 코스는 완료로 치지 않는다. */
    public boolean isCourseFullyCompleted(Long memberId, Long courseId) {
        List<Long> courseLessonIds = lessonRepository.findByCourseIdOrderByOrderNoAsc(courseId).stream()
                .map(Lesson::getId)
                .toList();
        if (courseLessonIds.isEmpty()) {
            return false;
        }
        Set<Long> completedLessonIds = learningProgressRepository.findByMemberId(memberId).stream()
                .filter(LearningProgress::isCompleted)
                .map(LearningProgress::getLessonId)
                .collect(Collectors.toSet());
        return completedLessonIds.containsAll(courseLessonIds);
    }

    @Transactional
    public void complete(Long memberId, Long lessonId) {
        memberService.getMember(memberId);
        if (!lessonRepository.existsById(lessonId)) {
            throw new LessonNotFoundException(lessonId);
        }

        LearningProgress progress = learningProgressRepository.findByMemberIdAndLessonId(memberId, lessonId)
                .orElseGet(() -> LearningProgress.builder()
                        .memberId(memberId)
                        .lessonId(lessonId)
                        .build());

        if (!progress.isCompleted()) {
            progress.complete();
        }

        learningProgressRepository.save(progress);
    }

    /**
     * 완료한 지 {@link #REVIEW_DUE_AFTER_DAYS}일이 지난 레슨을 오래된 순으로 최대
     * {@link #REVIEW_LIST_LIMIT}개까지 반환한다 — 새 추적 데이터 없이 기존 {@code completedAt}만 재사용한다.
     */
    public List<ReviewLessonResponse> getLessonsDueForReview(Long memberId) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(REVIEW_DUE_AFTER_DAYS);
        List<LearningProgress> due = learningProgressRepository
                .findByMemberIdAndCompletedTrueAndCompletedAtBeforeOrderByCompletedAtAsc(memberId, cutoff)
                .stream()
                .limit(REVIEW_LIST_LIMIT)
                .toList();
        if (due.isEmpty()) {
            return List.of();
        }

        List<Long> lessonIds = due.stream().map(LearningProgress::getLessonId).toList();
        Map<Long, Lesson> lessonsById = lessonRepository.findAllById(lessonIds).stream()
                .collect(Collectors.toMap(Lesson::getId, lesson -> lesson));

        List<Long> courseIds = lessonsById.values().stream().map(Lesson::getCourseId).distinct().toList();
        Map<Long, Course> coursesById = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, course -> course));

        return due.stream()
                .map(progress -> {
                    Lesson lesson = lessonsById.get(progress.getLessonId());
                    if (lesson == null) {
                        return null;
                    }
                    Course course = coursesById.get(lesson.getCourseId());
                    return new ReviewLessonResponse(
                            lesson.getId(), lesson.getTitle(),
                            course.getId(), course.getTitle(),
                            progress.getCompletedAt());
                })
                .filter(response -> response != null)
                .toList();
    }

    /** 헤더 배지용 — 레슨/코스 배치 조회 없이 개수만 필요할 때 쓴다. 상한은 {@link #getLessonsDueForReview}와 동일. */
    public long countLessonsDueForReview(Long memberId) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(REVIEW_DUE_AFTER_DAYS);
        return learningProgressRepository
                .findByMemberIdAndCompletedTrueAndCompletedAtBeforeOrderByCompletedAtAsc(memberId, cutoff)
                .stream()
                .limit(REVIEW_LIST_LIMIT)
                .count();
    }

    /** 복습했다는 표시로 완료 시각을 지금으로 밀어낸다 — 멱등성 가드 없이 매번 갱신하는 게 목적. */
    @Transactional
    public void markReviewed(Long memberId, Long lessonId) {
        LearningProgress progress = learningProgressRepository.findByMemberIdAndLessonId(memberId, lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));
        progress.complete();
        learningProgressRepository.save(progress);
    }

    public List<CourseProgressResponse> getCourseProgress(Long memberId) {
        List<LearningProgress> memberProgress = learningProgressRepository.findByMemberId(memberId);
        if (memberProgress.isEmpty()) {
            return List.of();
        }

        List<Long> touchedLessonIds = memberProgress.stream().map(LearningProgress::getLessonId).toList();
        Map<Long, Lesson> lessonsById = lessonRepository.findAllById(touchedLessonIds).stream()
                .collect(Collectors.toMap(Lesson::getId, lesson -> lesson));

        Map<Long, Long> completedCountByCourse = memberProgress.stream()
                .filter(LearningProgress::isCompleted)
                .map(progress -> lessonsById.get(progress.getLessonId()))
                .filter(lesson -> lesson != null)
                .collect(Collectors.groupingBy(Lesson::getCourseId, Collectors.counting()));

        List<Long> touchedCourseIds = lessonsById.values().stream()
                .map(Lesson::getCourseId)
                .distinct()
                .toList();

        Map<Long, Course> coursesById = courseRepository.findAllById(touchedCourseIds).stream()
                .collect(Collectors.toMap(Course::getId, course -> course));
        Map<Long, Long> totalLessonCountByCourse = lessonRepository.findByCourseIdIn(touchedCourseIds).stream()
                .collect(Collectors.groupingBy(Lesson::getCourseId, Collectors.counting()));

        return touchedCourseIds.stream()
                .map(courseId -> {
                    Course course = coursesById.get(courseId);
                    int totalLessons = totalLessonCountByCourse.getOrDefault(courseId, 0L).intValue();
                    int completedLessons = completedCountByCourse.getOrDefault(courseId, 0L).intValue();

                    return new CourseProgressResponse(
                            course.getId(),
                            course.getTitle(),
                            course.getEmoji(),
                            totalLessons,
                            completedLessons
                    );
                })
                .sorted(Comparator.comparing(CourseProgressResponse::courseId))
                .toList();
    }

    /**
     * 학습 이력을 근거로 비중을 둘 영역을 추천한다.
     * LearningProgress는 완료 여부만 기록하고(정답률·오답 데이터 없음, 퀴즈 미도입) 완료된 순간에만 생성되므로
     * "완료율"은 항상 100%라 신호가 되지 않는다. 대신 "회원 레벨에 존재하는 레슨 대비 얼마나 진행했는지(커버리지)"를
     * 약점의 대리 지표로 쓴다 — 커버리지가 가장 낮은 영역이 상대적으로 손대지 않은, 즉 더 필요한 영역이라고 본다.
     * 커버리지가 가장 낮은 영역(들)을 반환한다 — 여러 영역이 동률이면 모두 포함한다.
     */
    public Set<LessonType> recommendFocusAreas(Long memberId) {
        MemberResponse member = memberService.getMember(memberId);

        long completedCount = learningProgressRepository.findByMemberId(memberId).stream()
                .filter(LearningProgress::isCompleted)
                .count();
        if (completedCount < MIN_HISTORY_LESSONS) {
            throw new InsufficientHistoryException();
        }

        Map<LessonType, SkillAreaCounts> counts = computeSkillAreaCounts(memberId, member);
        if (counts.isEmpty()) {
            throw new InsufficientHistoryException();
        }

        double lowestCoverage = counts.values().stream()
                .mapToDouble(SkillAreaCounts::coverage)
                .min()
                .orElseThrow();

        Set<LessonType> focusAreas = EnumSet.noneOf(LessonType.class);
        counts.forEach((type, c) -> {
            if (c.coverage() == lowestCoverage) {
                focusAreas.add(type);
            }
        });

        return focusAreas;
    }

    /**
     * 영역별(어휘/읽기/쓰기/듣기/말하기) 완료 레슨 수 대비 회원 레벨에 존재하는 전체 레슨 수를 반환한다.
     * 마이페이지 대시보드의 "영역별 학습 현황" 막대그래프에 쓰인다.
     */
    public List<SkillAreaProgressResponse> getSkillAreaProgress(Long memberId) {
        MemberResponse member = memberService.getMember(memberId);
        Map<LessonType, SkillAreaCounts> counts = computeSkillAreaCounts(memberId, member);

        return Arrays.stream(LessonType.values())
                .filter(counts::containsKey)
                .map(type -> {
                    SkillAreaCounts c = counts.get(type);
                    return new SkillAreaProgressResponse(type, (int) c.completed(), (int) c.available());
                })
                .toList();
    }

    /** 마이페이지 대시보드 상단 요약 통계(완료 레슨 수/학습 중인 코스 수/전체 진도율/연속 학습일). */
    public DashboardSummaryResponse getDashboardSummary(Long memberId) {
        List<CourseProgressResponse> courseProgress = getCourseProgress(memberId);
        int totalCompleted = courseProgress.stream().mapToInt(CourseProgressResponse::completedLessons).sum();
        int totalLessons = courseProgress.stream().mapToInt(CourseProgressResponse::totalLessons).sum();
        int overallPercentage = totalLessons == 0 ? 0 : (int) Math.round(totalCompleted * 100.0 / totalLessons);

        return new DashboardSummaryResponse(totalCompleted, courseProgress.size(), overallPercentage, getCurrentStreak(memberId));
    }

    /**
     * 오늘까지 연속으로 학습한 날짜 수. 새 추적 데이터 없이 기존 completedAt만으로 즉석 계산한다(stateless).
     * 오늘 아직 학습을 안 했어도 어제까지 이어져 있으면 스트릭이 살아있는 것으로 본다(하루 유예).
     */
    public int getCurrentStreak(Long memberId) {
        Set<LocalDate> activeDates = learningProgressRepository.findByMemberId(memberId).stream()
                .filter(LearningProgress::isCompleted)
                .map(progress -> progress.getCompletedAt().toLocalDate())
                .collect(Collectors.toSet());
        if (activeDates.isEmpty()) {
            return 0;
        }

        LocalDate cursor = LocalDate.now();
        if (!activeDates.contains(cursor)) {
            cursor = cursor.minusDays(STREAK_GRACE_PERIOD_DAYS);
            if (!activeDates.contains(cursor)) {
                return 0;
            }
        }

        int streak = 0;
        while (activeDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /** 이번 달 1일부터 말일까지 일별 완료 레슨 수. 마이페이지 활동 캘린더에 쓰인다. */
    public List<DailyActivityResponse> getMonthlyActivity(Long memberId) {
        Map<LocalDate, Long> countsByDate = learningProgressRepository.findByMemberId(memberId).stream()
                .filter(LearningProgress::isCompleted)
                .collect(Collectors.groupingBy(
                        progress -> progress.getCompletedAt().toLocalDate(), Collectors.counting()));

        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());
        return start.datesUntil(end.plusDays(1))
                .map(date -> new DailyActivityResponse(
                        date, dayLabel(date), countsByDate.getOrDefault(date, 0L).intValue()))
                .toList();
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

    /** 영역별 완료/가능 레슨 수. {@link #recommendFocusAreas}와 {@link #getSkillAreaProgress}가 공유한다. */
    private Map<LessonType, SkillAreaCounts> computeSkillAreaCounts(Long memberId, MemberResponse member) {
        List<Long> completedLessonIds = learningProgressRepository.findByMemberId(memberId).stream()
                .filter(LearningProgress::isCompleted)
                .map(LearningProgress::getLessonId)
                .toList();
        Map<LessonType, Long> completedCountByType = lessonRepository.findAllById(completedLessonIds).stream()
                .collect(Collectors.groupingBy(Lesson::getLessonType, Collectors.counting()));

        List<Course> officialCourses = courseRepository.search(member.memberType(), member.level(), null, null);
        Map<LessonType, Long> availableCountByType = lessonRepository
                .findByCourseIdIn(officialCourses.stream().map(Course::getId).toList()).stream()
                .collect(Collectors.groupingBy(Lesson::getLessonType, Collectors.counting()));

        Map<LessonType, SkillAreaCounts> result = new EnumMap<>(LessonType.class);
        availableCountByType.forEach((type, available) ->
                result.put(type, new SkillAreaCounts(completedCountByType.getOrDefault(type, 0L), available)));
        return result;
    }

    private record SkillAreaCounts(long completed, long available) {
        double coverage() {
            return available == 0 ? 0.0 : completed / (double) available;
        }
    }
}
