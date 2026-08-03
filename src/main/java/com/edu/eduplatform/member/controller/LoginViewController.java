package com.edu.eduplatform.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginViewController {

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String resetSuccess,
                             @RequestParam(required = false) String withdrawn,
                             Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        model.addAttribute("resetSuccess", resetSuccess != null);
        model.addAttribute("withdrawn", withdrawn != null);
        return "member/login-form";
    }
}
