package com.edu.eduplatform.course.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.course.dto.CourseResponse;
import com.edu.eduplatform.course.dto.PersonalCourseCreationResult;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.exception.InvalidFocusAreasException;
import com.edu.eduplatform.course.service.CourseService;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.dto.LessonSummaryResponse;
import com.edu.eduplatform.lesson.service.LessonService;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import com.edu.eduplatform.progress.exception.InsufficientHistoryException;
import com.edu.eduplatform.progress.service.ProgressService;
import com.edu.eduplatform.question.exception.DiagnosticTestIncompleteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseViewController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final ProgressService progressService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        MemberType targetType = parseEnum(MemberType.class, target);
        EnglishLevel levelFilter = parseEnum(EnglishLevel.class, level);
        LessonType typeFilter = parseEnum(LessonType.class, type);
        String keywordFilter = StringUtils.hasText(keyword) ? keyword.strip() : null;

        model.addAttribute("courses", courseService.list(targetType, levelFilter, typeFilter, keywordFilter));
        model.addAttribute("memberTypes", MemberType.values());
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("selectedTarget", target);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("selectedSkillType", type);
        model.addAttribute("selectedKeyword", keyword);
        return "course/list";
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(required = false) String type,
            @CurrentMemberId Long memberId,
            Model model
    ) {
        try {
            CourseResponse course = courseService.getCourse(id);
            model.addAttribute("course", course);

            List<LessonSummaryResponse> allLessons = lessonService.listByCourse(id);
            LessonType selectedType = parseEnum(LessonType.class, type);
            List<LessonSummaryResponse> lessons = selectedType == null
                    ? allLessons
                    : allLessons.stream().filter(lesson -> lesson.lessonType() == selectedType).toList();

            Map<String, Long> lessonCounts = allLessons.stream()
                    .collect(Collectors.groupingBy(lesson -> lesson.lessonType().name(), Collectors.counting()));

            model.addAttribute("lessons", lessons);
            model.addAttribute("lessonTypes", LessonType.values());
            model.addAttribute("lessonCounts", lessonCounts);
            model.addAttribute("selectedType", type);

            if (memberId != null && !course.isPersonal()) {
                boolean courseCompleted = progressService.isCourseFullyCompleted(memberId, id);
                model.addAttribute("courseCompleted", courseCompleted);
                if (courseCompleted) {
                    model.addAttribute("nextCourse", courseService.recommendNextCourse(memberId, id).orElse(null));
                }
            }
            return "course/detail";
        } catch (CourseNotFoundException e) {
            return "redirect:/courses";
        }
    }

    @GetMapping("/personal/new")
    public String personalCourseForm(Model model) {
        model.addAttribute("lessonTypes", LessonType.values());
        return "course/personal-new";
    }

    @PostMapping("/personal")
    public String createPersonalCourse(
            @CurrentMemberId Long memberId,
            @RequestParam(required = false) Set<LessonType> focusAreas,
            Model model
    ) {
        try {
            PersonalCourseCreationResult result = courseService.createPersonalCourse(memberId, focusAreas);
            return "redirect:/courses/" + result.course().id();
        } catch (MemberNotFoundException e) {
            return "redirect:/members/new";
        } catch (InvalidFocusAreasException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("lessonTypes", LessonType.values());
            return "course/personal-new";
        }
    }

    @PostMapping("/personal/history-based")
    public String createPersonalCourseFromHistory(@CurrentMemberId Long memberId, Model model) {
        try {
            PersonalCourseCreationResult result = courseService.createPersonalCourseFromHistory(memberId);
            return "redirect:/courses/" + result.course().id();
        } catch (MemberNotFoundException e) {
            return "redirect:/members/new";
        } catch (InsufficientHistoryException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("lessonTypes", LessonType.values());
            return "course/personal-new";
        }
    }

    @GetMapping("/personal/diagnostic-test")
    public String diagnosticTestForm(@CurrentMemberId Long memberId, Model model) {
        model.addAttribute("questions", courseService.getDiagnosticTestQuestions(memberId));
        return "course/diagnostic-test";
    }

    @PostMapping("/personal/diagnostic-test")
    public String createPersonalCourseFromDiagnosticTest(
            @CurrentMemberId Long memberId, @RequestParam Map<String, String> params, Model model
    ) {
        try {
            PersonalCourseCreationResult result = courseService.createPersonalCourseFromDiagnosticTest(memberId, parseAnswers(params));
            return "redirect:/courses/" + result.course().id();
        } catch (MemberNotFoundException e) {
            return "redirect:/members/new";
        } catch (DiagnosticTestIncompleteException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("questions", courseService.getDiagnosticTestQuestions(memberId));
            return "course/diagnostic-test";
        }
    }

    /** 라디오 그룹 name="answer-{questionId}" 로 제출된 값들을 questionId → 선택한 보기 인덱스로 모은다. */
    private static Map<Long, Integer> parseAnswers(Map<String, String> params) {
        Map<Long, Integer> answers = new HashMap<>();
        params.forEach((key, value) -> {
            if (key.startsWith("answer-")) {
                answers.put(Long.valueOf(key.substring("answer-".length())), Integer.valueOf(value));
            }
        });
        return answers;
    }

    /** 잘못된 필터 값(예: 오타 섞인 쿼리 파라미터)은 500 대신 "필터 없음"으로 조용히 무시한다. */
    private static <E extends Enum<E>> E parseEnum(Class<E> enumType, String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
