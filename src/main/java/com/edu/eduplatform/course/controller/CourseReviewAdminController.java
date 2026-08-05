package com.edu.eduplatform.course.controller;

import com.edu.eduplatform.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/** 관리자 전용 리뷰 관리 화면 — SecurityConfig가 /admin/** 을 ROLE_ADMIN으로 이미 막아둔다. */
@Controller
@RequiredArgsConstructor
public class CourseReviewAdminController {

    private final CourseService courseService;

    @GetMapping("/admin/reviews")
    public String list(Model model) {
        model.addAttribute("reviews", courseService.listAllReviewsForAdmin());
        return "admin/review-list";
    }

    @PostMapping("/admin/reviews/{id}/delete")
    public String delete(@PathVariable Long id) {
        courseService.deleteReviewByAdmin(id);
        return "redirect:/admin/reviews";
    }
}
