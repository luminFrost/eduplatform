package com.edu.eduplatform.lesson.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.service.LessonService;
import com.edu.eduplatform.progress.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lessons")
public class LessonViewController {

    private final LessonService lessonService;
    private final ProgressService progressService;

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @CurrentMemberId Long memberId, Model model) {
        try {
            model.addAttribute("lesson", lessonService.getDetail(id));
            model.addAttribute("currentMemberId", memberId);
            model.addAttribute("completed", memberId != null && progressService.isCompleted(memberId, id));
            return "lesson/detail";
        } catch (LessonNotFoundException e) {
            return "redirect:/courses";
        }
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, @CurrentMemberId Long memberId) {
        if (memberId == null) {
            return "redirect:/members/new";
        }

        try {
            progressService.complete(memberId, id);
        } catch (LessonNotFoundException e) {
            return "redirect:/courses";
        }

        return "redirect:/lessons/" + id;
    }
}
