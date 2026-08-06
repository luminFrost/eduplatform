package com.edu.eduplatform.progress.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.course.service.CourseService;
import com.edu.eduplatform.member.service.MemberService;
import com.edu.eduplatform.progress.dto.DailyActivityResponse;
import com.edu.eduplatform.progress.service.ProgressService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final MemberService memberService;
    private final ProgressService progressService;
    private final CourseService courseService;

    @GetMapping("/my")
    public String dashboard(@CurrentMemberId Long memberId, Model model) {
        model.addAttribute("member", memberService.getMember(memberId));
        model.addAttribute("summary", progressService.getDashboardSummary(memberId));
        model.addAttribute("skillAreaProgress", progressService.getSkillAreaProgress(memberId));
        model.addAttribute("courseProgress", progressService.getCourseProgress(memberId));
        model.addAttribute("personalCourses", courseService.listPersonalCourses(memberId));
        model.addAttribute("bookmarkedCourses", courseService.listBookmarkedCourses(memberId));

        List<DailyActivityResponse> monthlyActivity = progressService.getMonthlyActivity(memberId);
        model.addAttribute("monthlyActivity", monthlyActivity);
        model.addAttribute("leadingBlanks", monthlyActivity.get(0).date().getDayOfWeek().getValue() - 1);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("recentActivity", progressService.getRecentActivity(memberId));
        model.addAttribute("weeklyGoalProgress", progressService.getWeeklyGoalProgress(memberId));
        return "my/dashboard";
    }
}
