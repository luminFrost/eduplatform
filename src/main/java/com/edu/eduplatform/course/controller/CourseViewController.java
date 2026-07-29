package com.edu.eduplatform.course.controller;

import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.service.CourseService;
import com.edu.eduplatform.lesson.service.LessonService;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            Model model
    ) {
        MemberType targetType = StringUtils.hasText(target) ? MemberType.valueOf(target) : null;
        EnglishLevel levelFilter = StringUtils.hasText(level) ? EnglishLevel.valueOf(level) : null;

        model.addAttribute("courses", courseService.list(targetType, levelFilter));
        model.addAttribute("memberTypes", MemberType.values());
        model.addAttribute("levels", EnglishLevel.values());
        model.addAttribute("selectedTarget", target);
        model.addAttribute("selectedLevel", level);
        return "course/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("course", courseService.getCourse(id));
            model.addAttribute("lessons", lessonService.listByCourse(id));
            return "course/detail";
        } catch (CourseNotFoundException e) {
            return "redirect:/courses";
        }
    }
}
