package com.edu.eduplatform.lesson.controller;

import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.lesson.dto.LessonAdminRequest;
import com.edu.eduplatform.lesson.dto.LessonAdminResponse;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.service.LessonService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/** 관리자 전용 레슨 관리 화면 — SecurityConfig가 /admin/** 을 ROLE_ADMIN으로 이미 막아둔다. */
@Controller
@RequiredArgsConstructor
public class LessonAdminController {

    private final LessonService lessonService;

    @GetMapping("/admin/courses/{courseId}/lessons/new")
    public String newForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("lessonTypes", LessonType.values());
        return "admin/lesson-form";
    }

    @PostMapping("/admin/courses/{courseId}/lessons/new")
    public String create(@PathVariable Long courseId, @Valid LessonAdminRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", validationMessages(bindingResult));
            model.addAttribute("courseId", courseId);
            model.addAttribute("form", request);
            model.addAttribute("lessonTypes", LessonType.values());
            return "admin/lesson-form";
        }
        try {
            lessonService.createLesson(courseId, request);
            return "redirect:/admin/courses/" + courseId;
        } catch (CourseNotFoundException e) {
            return "redirect:/admin/courses";
        }
    }

    @GetMapping("/admin/lessons/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            LessonAdminResponse lesson = lessonService.getLessonForEdit(id);
            model.addAttribute("lessonId", id);
            model.addAttribute("courseId", lesson.courseId());
            model.addAttribute("form", new LessonAdminRequest(
                    lesson.title(), lesson.orderNo(), lesson.content(), lesson.lessonType()));
            model.addAttribute("lessonTypes", LessonType.values());
            return "admin/lesson-form";
        } catch (LessonNotFoundException e) {
            return "redirect:/admin/courses";
        }
    }

    @PostMapping("/admin/lessons/{id}/edit")
    public String edit(@PathVariable Long id, @Valid LessonAdminRequest request, BindingResult bindingResult, Model model) {
        Long courseId = resolveCourseId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", validationMessages(bindingResult));
            model.addAttribute("lessonId", id);
            model.addAttribute("courseId", courseId);
            model.addAttribute("form", request);
            model.addAttribute("lessonTypes", LessonType.values());
            return "admin/lesson-form";
        }
        try {
            lessonService.updateLesson(id, request);
            return "redirect:/admin/courses/" + courseId;
        } catch (LessonNotFoundException e) {
            return "redirect:/admin/courses";
        }
    }

    @PostMapping("/admin/lessons/{id}/delete")
    public String delete(@PathVariable Long id) {
        Long courseId = resolveCourseId(id);
        try {
            lessonService.deleteLesson(id);
        } catch (LessonNotFoundException e) {
            // 이미 없는 레슨 — 그대로 코스로 돌아간다.
        }
        return courseId != null ? "redirect:/admin/courses/" + courseId : "redirect:/admin/courses";
    }

    private Long resolveCourseId(Long lessonId) {
        try {
            return lessonService.getLessonForEdit(lessonId).courseId();
        } catch (LessonNotFoundException e) {
            return null;
        }
    }

    private static List<String> validationMessages(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }
}
