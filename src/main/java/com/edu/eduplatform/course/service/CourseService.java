package com.edu.eduplatform.course.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.domain.CourseBookmark;
import com.edu.eduplatform.course.domain.CourseCriteriaSource;
import com.edu.eduplatform.course.domain.CourseReview;
import com.edu.eduplatform.course.dto.CourseCreateRequest;
import com.edu.eduplatform.course.dto.CourseRatingSummary;
import com.edu.eduplatform.course.dto.CourseResponse;
import com.edu.eduplatform.course.dto.CourseReviewResponse;
import com.edu.eduplatform.course.dto.PersonalCourseCreationResult;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.exception.InvalidFocusAreasException;
import com.edu.eduplatform.course.exception.InvalidReviewException;
import com.edu.eduplatform.course.repository.CourseBookmarkRepository;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.course.repository.CourseReviewRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.member.service.MemberService;
import com.edu.eduplatform.progress.service.ProgressService;
import com.edu.eduplatform.question.dto.QuestionResponse;
import com.edu.eduplatform.question.service.QuestionService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final MemberService memberService;
    private final ProgressService progressService;
    private final CourseReviewRepository courseReviewRepository;
    private final MemberRepository memberRepository;
    private final QuestionService questionService;
    private final CourseBookmarkRepository courseBookmarkRepository;

    public List<CourseResponse> list(MemberType targetType, EnglishLevel level, LessonType lessonType, String keyword) {
        return courseRepository.search(targetType, level, lessonType, keyword).stream()
                .map(CourseResponse::from)
                .toList();
    }

    public List<CourseResponse> listPersonalCourses(Long memberId) {
        return courseRepository.findByOwnerIdOrderByIdDesc(memberId).stream()
                .map(CourseResponse::from)
                .toList();
    }

    @Transactional
    public boolean toggleBookmark(Long memberId, Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException(courseId);
        }
        if (courseBookmarkRepository.existsByMemberIdAndCourseId(memberId, courseId)) {
            courseBookmarkRepository.deleteByMemberIdAndCourseId(memberId, courseId);
            return false;
        }
        courseBookmarkRepository.save(CourseBookmark.builder().memberId(memberId).courseId(courseId).build());
        return true;
    }

    public boolean isBookmarked(Long memberId, Long courseId) {
        return memberId != null && courseBookmarkRepository.existsByMemberIdAndCourseId(memberId, courseId);
    }

    /** courseRepository.findAllById()는 입력 순서를 보장하지 않아 북마크 최신순으로 직접 재정렬한다. */
    public List<CourseResponse> listBookmarkedCourses(Long memberId) {
        List<Long> orderedCourseIds = courseBookmarkRepository.findByMemberIdOrderByIdDesc(memberId).stream()
                .map(CourseBookmark::getCourseId)
                .toList();
        Map<Long, Course> coursesById = courseRepository.findAllById(orderedCourseIds).stream()
                .collect(Collectors.toMap(Course::getId, course -> course));
        return orderedCourseIds.stream()
                .map(coursesById::get)
                .filter(Objects::nonNull)
                .map(CourseResponse::from)
                .toList();
    }

    @Transactional
    public CourseReviewResponse submitReview(Long memberId, Long courseId, int rating, String comment) {
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException(courseId);
        }
        if (rating < 1 || rating > 5) {
            throw new InvalidReviewException("별점은 1~5 사이여야 합니다.");
        }
        if (comment != null && comment.length() > 500) {
            throw new InvalidReviewException("후기는 500자 이내로 입력해 주세요.");
        }

        CourseReview review = courseReviewRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseGet(() -> CourseReview.builder().memberId(memberId).courseId(courseId).rating(rating).comment(comment).build());
        review.update(rating, comment);
        CourseReview saved = courseReviewRepository.save(review);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        return toReviewResponse(saved, member.getNickname());
    }

    @Transactional
    public void deleteReview(Long memberId, Long courseId) {
        courseReviewRepository.deleteByMemberIdAndCourseId(memberId, courseId);
    }

    /** 리뷰마다 회원을 따로 조회하지 않도록 닉네임을 배치 조회해 매칭한다. */
    public List<CourseReviewResponse> listReviews(Long courseId) {
        List<CourseReview> reviews = courseReviewRepository.findByCourseIdOrderByIdDesc(courseId);
        Map<Long, String> nicknamesById = memberRepository.findAllById(
                        reviews.stream().map(CourseReview::getMemberId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));
        return reviews.stream()
                .map(r -> toReviewResponse(r, nicknamesById.getOrDefault(r.getMemberId(), "알 수 없음")))
                .toList();
    }

    public Optional<CourseReviewResponse> getMyReview(Long memberId, Long courseId) {
        return courseReviewRepository.findByMemberIdAndCourseId(memberId, courseId)
                .map(r -> toReviewResponse(r, null));
    }

    public CourseRatingSummary getRatingSummary(Long courseId) {
        Double average = courseReviewRepository.findAverageRatingByCourseId(courseId);
        return new CourseRatingSummary(average != null ? average : 0.0, courseReviewRepository.countByCourseId(courseId));
    }

    /** 코스 목록 화면에서 카드마다 따로 집계 쿼리를 날리지 않도록 courseId 여러 개를 한 번에 집계한다. */
    public Map<Long, CourseRatingSummary> getRatingSummaries(Collection<Long> courseIds) {
        return courseReviewRepository.findRatingSummaries(courseIds).stream()
                .collect(Collectors.toMap(
                        CourseReviewRepository.CourseRatingProjection::getCourseId,
                        p -> new CourseRatingSummary(p.getAverageRating(), p.getReviewCount())));
    }

    private static CourseReviewResponse toReviewResponse(CourseReview review, String nickname) {
        return new CourseReviewResponse(review.getId(), review.getMemberId(), nickname,
                review.getRating(), review.getComment(), review.getCreatedAt());
    }

    public CourseResponse getCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));

        return CourseResponse.from(course);
    }

    /**
     * 코스를 완료한 회원에게 같은 대상(target)의 로드맵에서 다음으로 들을 코스를 추천한다.
     * 공식 코스는 id 오름차순이 곧 로드맵 순서다(BEGINNER→ADVANCED, 레벨 안에서도 저장 순서 — 검증됨).
     * 현재 코스 다음부터 훑어 아직 다 안 끝낸 코스 중 첫 번째를 반환하고, 로드맵을 전부 마쳤거나 현재
     * 코스가 개인 코스라 로드맵에 없으면 빈 값을 반환한다.
     */
    public Optional<CourseResponse> recommendNextCourse(Long memberId, Long currentCourseId) {
        Course current = courseRepository.findById(currentCourseId)
                .orElseThrow(() -> new CourseNotFoundException(currentCourseId));
        List<Course> roadmap = courseRepository.search(current.getTargetType(), null, null, null);
        int currentIndex = IntStream.range(0, roadmap.size())
                .filter(i -> roadmap.get(i).getId().equals(currentCourseId))
                .findFirst()
                .orElse(-1);
        if (currentIndex == -1) {
            // 개인 코스 등 로드맵(공식 코스 목록)에 없는 코스 — 다음 코스 개념이 없다.
            return Optional.empty();
        }

        for (int i = currentIndex + 1; i < roadmap.size(); i++) {
            Course candidate = roadmap.get(i);
            if (!progressService.isCourseFullyCompleted(memberId, candidate.getId())) {
                return Optional.of(CourseResponse.from(candidate));
            }
        }
        return Optional.empty();
    }

    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .emoji(request.emoji())
                .targetType(request.targetType())
                .level(request.level())
                .build();

        return CourseResponse.from(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseCreateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        course.updateDetails(request.title(), request.description(), request.emoji(), request.targetType(), request.level());
        courseRepository.save(course);
        return CourseResponse.from(course);
    }

    /**
     * 자가 선택(SELF_SELECTED) 기준으로 개인 코스를 만든다.
     * 회원과 같은 대상·레벨의 공식 코스들에서 선택한 영역(focusAreas)에 해당하는 레슨을 그대로 복사해 담는다
     * (레슨을 여러 코스가 공유하지 않는다 — PRODUCT.md 3-2).
     */
    @Transactional
    public PersonalCourseCreationResult createPersonalCourse(Long memberId, Set<LessonType> focusAreas) {
        if (focusAreas == null || focusAreas.isEmpty()) {
            throw new InvalidFocusAreasException();
        }

        MemberResponse member = memberService.getMember(memberId);
        return buildPersonalCourse(member, focusAreas, CourseCriteriaSource.SELF_SELECTED,
                "선택한 영역(" + describeFocusAreas(focusAreas) + ")에 맞춰 자동으로 구성된 나만의 코스입니다.");
    }

    /**
     * 학습 이력 기반(HISTORY_BASED) 기준으로 개인 코스를 만든다.
     * 완료율이 가장 낮은 영역(들)을 {@link ProgressService#recommendFocusAreas}가 추천하면, 그대로
     * 자가 선택과 동일한 방식으로 레슨을 복사해 코스를 구성한다.
     */
    @Transactional
    public PersonalCourseCreationResult createPersonalCourseFromHistory(Long memberId) {
        MemberResponse member = memberService.getMember(memberId);
        Set<LessonType> focusAreas = progressService.recommendFocusAreas(memberId);
        return buildPersonalCourse(member, focusAreas, CourseCriteriaSource.HISTORY_BASED,
                "학습 이력을 분석해 완료율이 낮은 영역(" + describeFocusAreas(focusAreas) + ") 위주로 자동 구성된 코스입니다.");
    }

    /** 회원의 대상·레벨에 맞는 진단 테스트 문항을 반환한다(정답 인덱스는 포함하지 않음). */
    public List<QuestionResponse> getDiagnosticTestQuestions(Long memberId) {
        MemberResponse member = memberService.getMember(memberId);
        return questionService.getDiagnosticTestQuestions(member.memberType(), member.level());
    }

    /**
     * 진단 테스트(DIAGNOSTIC_TEST) 기준으로 개인 코스를 만든다.
     * 정답률이 가장 낮은 영역(들)을 {@link QuestionService#determineFocusAreas}가 채점해 뽑으면, 그대로
     * 자가 선택과 동일한 방식으로 레슨을 복사해 코스를 구성한다.
     */
    @Transactional
    public PersonalCourseCreationResult createPersonalCourseFromDiagnosticTest(Long memberId, Map<Long, Integer> answersByQuestionId) {
        MemberResponse member = memberService.getMember(memberId);
        Set<LessonType> focusAreas = questionService.determineFocusAreas(member.memberType(), member.level(), answersByQuestionId);
        return buildPersonalCourse(member, focusAreas, CourseCriteriaSource.DIAGNOSTIC_TEST,
                "진단 테스트 결과 정답률이 낮았던 영역(" + describeFocusAreas(focusAreas) + ") 위주로 자동 구성된 코스입니다.");
    }

    /**
     * 이미 같은 focusAreas로 만든 개인 코스가 있으면(중복 제출 등) 새로 만들지 않고 그 코스를 그대로 반환한다.
     */
    private PersonalCourseCreationResult buildPersonalCourse(
            MemberResponse member, Set<LessonType> focusAreas, CourseCriteriaSource criteriaSource, String description
    ) {
        Optional<Course> existing = courseRepository.findByOwnerIdOrderByIdDesc(member.id()).stream()
                .filter(c -> c.getFocusAreas().equals(focusAreas))
                .findFirst();
        if (existing.isPresent()) {
            return new PersonalCourseCreationResult(CourseResponse.from(existing.get()), false);
        }

        List<Course> officialCourses = courseRepository.search(member.memberType(), member.level(), null, null);
        Map<Long, List<Lesson>> lessonsByCourseId = lessonRepository
                .findByCourseIdIn(officialCourses.stream().map(Course::getId).toList()).stream()
                .collect(Collectors.groupingBy(Lesson::getCourseId));
        List<Lesson> matchingLessons = officialCourses.stream()
                .flatMap(officialCourse -> lessonsByCourseId.getOrDefault(officialCourse.getId(), List.of()).stream())
                .filter(lesson -> focusAreas.contains(lesson.getLessonType()))
                .toList();

        Course personalCourse = courseRepository.save(Course.builder()
                .title(member.nickname() + "님의 맞춤 코스")
                .description(description)
                .emoji("🎯")
                .targetType(member.memberType())
                .level(member.level())
                .ownerId(member.id())
                .focusAreas(focusAreas)
                .criteriaSource(criteriaSource)
                .build());

        int orderNo = 1;
        for (Lesson source : matchingLessons) {
            lessonRepository.save(Lesson.builder()
                    .courseId(personalCourse.getId())
                    .orderNo(orderNo++)
                    .title(source.getTitle())
                    .content(source.getContent())
                    .lessonType(source.getLessonType())
                    .build());
        }

        return new PersonalCourseCreationResult(CourseResponse.from(personalCourse), true);
    }

    private String describeFocusAreas(Set<LessonType> focusAreas) {
        return focusAreas.stream()
                .map(Enum::name)
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
