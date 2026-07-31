package com.edu.eduplatform.course.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.domain.CourseCriteriaSource;
import com.edu.eduplatform.course.dto.CourseCreateRequest;
import com.edu.eduplatform.course.dto.CourseResponse;
import com.edu.eduplatform.course.dto.PersonalCourseCreateRequest;
import com.edu.eduplatform.course.dto.PersonalCourseCreationResult;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.exception.InvalidFocusAreasException;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.service.MemberService;
import com.edu.eduplatform.progress.service.ProgressService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    public List<CourseResponse> list(MemberType targetType, EnglishLevel level, LessonType lessonType) {
        return courseRepository.search(targetType, level, lessonType).stream()
                .map(CourseResponse::from)
                .toList();
    }

    public List<CourseResponse> listPersonalCourses(Long memberId) {
        return courseRepository.findByOwnerIdOrderByIdDesc(memberId).stream()
                .map(CourseResponse::from)
                .toList();
    }

    public CourseResponse getCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));

        return CourseResponse.from(course);
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

    /**
     * 자가 선택(SELF_SELECTED) 기준으로 개인 코스를 만든다.
     * 회원과 같은 대상·레벨의 공식 코스들에서 선택한 영역(focusAreas)에 해당하는 레슨을 그대로 복사해 담는다
     * (레슨을 여러 코스가 공유하지 않는다 — PRODUCT.md 3-2).
     */
    @Transactional
    public PersonalCourseCreationResult createPersonalCourse(PersonalCourseCreateRequest request) {
        if (request.focusAreas() == null || request.focusAreas().isEmpty()) {
            throw new InvalidFocusAreasException();
        }

        MemberResponse member = memberService.getMember(request.memberId());
        return buildPersonalCourse(member, request.focusAreas(), CourseCriteriaSource.SELF_SELECTED,
                "선택한 영역(" + describeFocusAreas(request.focusAreas()) + ")에 맞춰 자동으로 구성된 나만의 코스입니다.");
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

        List<Lesson> matchingLessons = courseRepository.search(member.memberType(), member.level(), null).stream()
                .flatMap(officialCourse -> lessonRepository.findByCourseIdOrderByOrderNoAsc(officialCourse.getId()).stream())
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
