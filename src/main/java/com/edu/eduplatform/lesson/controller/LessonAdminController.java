package com.edu.eduplatform.lesson.controller;

import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.lesson.dto.LessonAdminRequest;
import com.edu.eduplatform.lesson.dto.LessonAdminResponse;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.service.LessonService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** 관리자 전용 레슨 관리 화면 — SecurityConfig가 /admin/** 을 ROLE_ADMIN으로 이미 막아둔다. */
@Controller
@RequiredArgsConstructor
public class LessonAdminController {

    private final LessonService lessonService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

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

    @GetMapping("/admin/courses/{courseId}/lessons/import")
    public String importForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "admin/lesson-import";
    }

    @PostMapping("/admin/courses/{courseId}/lessons/import")
    public String importLessons(@PathVariable Long courseId, @RequestParam String json, Model model) {
        List<LessonAdminRequest> parsed;
        try {
            List<LessonDraft> drafts = objectMapper.readValue(json, new TypeReference<List<LessonDraft>>() {
            });
            parsed = drafts.stream()
                    .map(draft -> new LessonAdminRequest(draft.title(), 0, draft.content(), draft.lessonType()))
                    .toList();
        } catch (RuntimeException e) {
            model.addAttribute("validationErrors", List.of("JSON 형식을 확인해 주세요 — 배열 형태여야 해요."));
            model.addAttribute("courseId", courseId);
            model.addAttribute("rawJson", json);
            return "admin/lesson-import";
        }

        List<String> errors = validateAll(parsed);
        if (parsed.isEmpty()) {
            errors.add("최소 1개 이상 입력해 주세요.");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("validationErrors", errors);
            model.addAttribute("courseId", courseId);
            model.addAttribute("rawJson", json);
            return "admin/lesson-import";
        }

        try {
            lessonService.createLessons(courseId, parsed);
            return "redirect:/admin/courses/" + courseId;
        } catch (CourseNotFoundException e) {
            return "redirect:/admin/courses";
        }
    }

    private List<String> validateAll(List<LessonAdminRequest> requests) {
        List<String> errors = new ArrayList<>();
        int rowNumber = 1;
        for (LessonAdminRequest request : requests) {
            for (ConstraintViolation<LessonAdminRequest> violation : validator.validate(request)) {
                errors.add(rowNumber + "번째 항목: " + violation.getMessage());
            }
            rowNumber++;
        }
        return errors;
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

    @PostMapping("/admin/lessons/{id}/move")
    public String move(@PathVariable Long id, @RequestParam String direction) {
        Long courseId = resolveCourseId(id);
        try {
            lessonService.moveLesson(id, direction);
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

    /**
     * JSON 일괄 등록 항목 하나의 모양 — {@link LessonAdminRequest}와 달리 orderNo가 없다.
     * orderNo는 {@link com.edu.eduplatform.lesson.service.LessonService#createLessons}가 항상
     * 서버에서 재계산해 덮어쓰므로 입력받지 않는다(primitive int라 JSON에서 아예 생략되면 Jackson이
     * 예외를 던지는 것도 피할 수 있다).
     */
    private record LessonDraft(String title, String content, LessonType lessonType) {
    }

    private static List<String> validationMessages(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }
}
