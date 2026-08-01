package com.edu.eduplatform.progress.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
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
@RequestMapping("/my/review")
public class ReviewViewController {

    private final ProgressService progressService;

    @GetMapping
    public String review(@CurrentMemberId Long memberId, Model model) {
        model.addAttribute("dueLessons", progressService.getLessonsDueForReview(memberId));
        return "my/review";
    }

    @PostMapping("/{lessonId}")
    public String markReviewed(@CurrentMemberId Long memberId, @PathVariable Long lessonId) {
        progressService.markReviewed(memberId, lessonId);
        return "redirect:/my/review";
    }
}
