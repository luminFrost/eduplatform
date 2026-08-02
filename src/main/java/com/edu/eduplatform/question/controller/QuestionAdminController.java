package com.edu.eduplatform.question.controller;

import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.dto.QuestionAdminRequest;
import com.edu.eduplatform.question.dto.QuestionAdminResponse;
import com.edu.eduplatform.question.exception.QuestionNotFoundException;
import com.edu.eduplatform.question.service.QuestionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 관리자 전용 진단 테스트 문항 관리 화면 — SecurityConfig가 /admin/** 을 ROLE_ADMIN으로 이미 막아둔다. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/questions")
public class QuestionAdminController {

    private final QuestionService questionService;

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

        List<QuestionAdminResponse> questions = questionService.getAllQuestions().stream()
                .filter(q -> targetType == null || q.targetType() == targetType)
                .filter(q -> levelFilter == null || q.level() == levelFilter)
                .filter(q -> typeFilter == null || q.lessonType() == typeFilter)
                .toList();

        model.addAttribute("questions", questions);
        model.addAttribute("memberTypes", MemberType.values());
        model.addAttribute("levels", EnglishLevel.values());
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("selectedTarget", target);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("selectedType", type);
        return "admin/question-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addFormOptions(model);
        return "admin/question-form";
    }

    @PostMapping("/new")
    public String create(@Valid QuestionAdminRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", validationMessages(bindingResult));
            model.addAttribute("form", request);
            addFormOptions(model);
            return "admin/question-form";
        }
        questionService.createQuestion(request);
        return "redirect:/admin/questions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            QuestionAdminResponse question = questionService.getQuestionForEdit(id);
            model.addAttribute("questionId", id);
            model.addAttribute("form", new QuestionAdminRequest(
                    question.targetType(), question.level(), question.lessonType(), question.prompt(),
                    question.audioText(),
                    question.options().get(0), question.options().get(1),
                    question.options().get(2), question.options().get(3),
                    question.correctOptionIndex()));
            addFormOptions(model);
            return "admin/question-form";
        } catch (QuestionNotFoundException e) {
            return "redirect:/admin/questions";
        }
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @Valid QuestionAdminRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", validationMessages(bindingResult));
            model.addAttribute("questionId", id);
            model.addAttribute("form", request);
            addFormOptions(model);
            return "admin/question-form";
        }
        try {
            questionService.updateQuestion(id, request);
            return "redirect:/admin/questions";
        } catch (QuestionNotFoundException e) {
            return "redirect:/admin/questions";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            questionService.deleteQuestion(id);
        } catch (QuestionNotFoundException e) {
            // 이미 없는 문항 — 그대로 목록으로 돌아간다.
        }
        return "redirect:/admin/questions";
    }

    private static List<String> validationMessages(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }

    private static void addFormOptions(Model model) {
        model.addAttribute("memberTypes", MemberType.values());
        model.addAttribute("levels", EnglishLevel.values());
        model.addAttribute("lessonTypes", LessonType.values());
    }

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
