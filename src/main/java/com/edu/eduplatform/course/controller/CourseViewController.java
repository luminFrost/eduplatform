package com.edu.eduplatform.course.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.course.dto.PersonalCourseCreateRequest;
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

    @GetMapping
    public String list(
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type,
            Model model
    ) {
        MemberType targetType = parseEnum(MemberType.class, target);
        EnglishLevel levelFilter = parseEnum(EnglishLevel.class, level);
        LessonType typeFilter = parseEnum(LessonType.class, type);

        model.addAttribute("courses", courseService.list(targetType, levelFilter, typeFilter));
        model.addAttribute("memberTypes", MemberType.values());
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("selectedTarget", target);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("selectedSkillType", type);
        return "course/list";
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(required = false) String type,
            Model model
    ) {
        try {
            model.addAttribute("course", courseService.getCourse(id));

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
            PersonalCourseCreationResult result = courseService.createPersonalCourse(new PersonalCourseCreateRequest(memberId, focusAreas));
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
