package com.edu.eduplatform.course.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.domain.CourseCriteriaSource;
import com.edu.eduplatform.course.dto.CourseCreateRequest;
import com.edu.eduplatform.course.dto.CourseResponse;
import com.edu.eduplatform.course.dto.PersonalCourseCreateRequest;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.service.MemberService;
import java.util.List;
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
    public CourseResponse createPersonalCourse(PersonalCourseCreateRequest request) {
        MemberResponse member = memberService.getMember(request.memberId());

        List<Lesson> matchingLessons = courseRepository.search(member.memberType(), member.level(), null).stream()
                .flatMap(officialCourse -> lessonRepository.findByCourseIdOrderByOrderNoAsc(officialCourse.getId()).stream())
                .filter(lesson -> request.focusAreas().contains(lesson.getLessonType()))
                .toList();

        Course personalCourse = courseRepository.save(Course.builder()
                .title(member.nickname() + "님의 맞춤 코스")
                .description("선택한 영역(" + describeFocusAreas(request) + ")에 맞춰 자동으로 구성된 나만의 코스입니다.")
                .emoji("🎯")
                .targetType(member.memberType())
                .level(member.level())
                .ownerId(member.id())
                .focusAreas(request.focusAreas())
                .criteriaSource(CourseCriteriaSource.SELF_SELECTED)
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

        return CourseResponse.from(personalCourse);
    }

    private String describeFocusAreas(PersonalCourseCreateRequest request) {
        return request.focusAreas().stream()
                .map(Enum::name)
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
