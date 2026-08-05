package com.edu.eduplatform.course.controller;

import com.edu.eduplatform.course.dto.CourseCreateRequest;
import com.edu.eduplatform.course.dto.CourseResponse;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.service.CourseService;
import com.edu.eduplatform.lesson.dto.LessonSummaryResponse;
import com.edu.eduplatform.lesson.service.LessonService;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
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
import org.springframework.web.bind.annotation.RequestMapping;

/** 관리자 전용 코스 관리 화면 — SecurityConfig가 /admin/** 을 ROLE_ADMIN으로 이미 막아둔다. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/courses")
public class CourseAdminController {

    private final CourseService courseService;
    private final LessonService lessonService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("courses", courseService.list(null, null, null, null, null));
        return "admin/course-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addFormOptions(model);
        return "admin/course-form";
    }

    @PostMapping("/new")
    public String create(@Valid CourseCreateRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", validationMessages(bindingResult));
            model.addAttribute("form", request);
            addFormOptions(model);
            return "admin/course-form";
        }
        CourseResponse created = courseService.create(request);
        return "redirect:/admin/courses/" + created.id();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            CourseResponse course = courseService.getCourse(id);
            List<LessonSummaryResponse> lessons = lessonService.listByCourse(id);
            model.addAttribute("course", course);
            model.addAttribute("lessons", lessons);
            return "admin/course-detail";
        } catch (CourseNotFoundException e) {
            return "redirect:/admin/courses";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            CourseResponse course = courseService.getCourse(id);
            model.addAttribute("courseId", id);
            model.addAttribute("form", new CourseCreateRequest(
                    course.title(), course.description(), course.emoji(), course.targetType(), course.level()));
            addFormOptions(model);
            return "admin/course-form";
        } catch (CourseNotFoundException e) {
            return "redirect:/admin/courses";
        }
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @Valid CourseCreateRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", validationMessages(bindingResult));
            model.addAttribute("courseId", id);
            model.addAttribute("form", request);
            addFormOptions(model);
            return "admin/course-form";
        }
        try {
            courseService.updateCourse(id, request);
            return "redirect:/admin/courses/" + id;
        } catch (CourseNotFoundException e) {
            return "redirect:/admin/courses";
        }
    }

    private static List<String> validationMessages(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }

    private static void addFormOptions(Model model) {
        model.addAttribute("memberTypes", MemberType.values());
        model.addAttribute("levels", EnglishLevel.values());
    }
}
