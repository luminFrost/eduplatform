package com.edu.eduplatform.lesson.controller;

import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lessons")
public class LessonViewController {

    private final LessonService lessonService;

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("lesson", lessonService.getDetail(id));
            return "lesson/detail";
        } catch (LessonNotFoundException e) {
            return "redirect:/courses";
        }
    }
}
