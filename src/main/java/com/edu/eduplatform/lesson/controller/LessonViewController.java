package com.edu.eduplatform.lesson.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.service.LessonService;
import com.edu.eduplatform.progress.service.ProgressService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String complete(@PathVariable Long id, @CurrentMemberId Long memberId,
                            @RequestParam(required = false) String quizAnswer, Model model) {
        try {
            Optional<String> correctAnswer = lessonService.deriveQuizAnswer(id);
            if (correctAnswer.isPresent() && !correctAnswer.get().equalsIgnoreCase(quizAnswer)) {
                model.addAttribute("lesson", lessonService.getDetail(id));
                model.addAttribute("currentMemberId", memberId);
                model.addAttribute("completed", false);
                model.addAttribute("quizError", "정답이 아니에요. 다시 골라보세요.");
                return "lesson/detail";
            }
            progressService.complete(memberId, id);
        } catch (LessonNotFoundException e) {
            return "redirect:/courses";
        }

        return "redirect:/lessons/" + id;
    }
}
