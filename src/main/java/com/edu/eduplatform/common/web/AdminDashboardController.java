package com.edu.eduplatform.common.web;

import com.edu.eduplatform.common.service.AdminStatsService;
import com.edu.eduplatform.common.service.ContentCoverageService;
import com.edu.eduplatform.lesson.domain.LessonType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 관리자 대시보드 — SecurityConfig가 /admin/** 을 ROLE_ADMIN으로 이미 막아둔다. */
@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ContentCoverageService contentCoverageService;
    private final AdminStatsService adminStatsService;

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("rows", contentCoverageService.getCoverageReport());
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("stats", adminStatsService.getSummary());
        return "admin/dashboard";
    }
}
