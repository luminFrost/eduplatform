package com.edu.eduplatform.progress.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.course.service.CourseService;
import com.edu.eduplatform.member.service.MemberService;
import com.edu.eduplatform.progress.service.ProgressService;
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
        model.addAttribute("courseProgress", progressService.getCourseProgress(memberId));
        model.addAttribute("personalCourses", courseService.listPersonalCourses(memberId));
        return "my/dashboard";
    }
}
