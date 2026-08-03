package com.edu.eduplatform.member.controller;

import com.edu.eduplatform.member.dto.PasswordResetConfirmRequest;
import com.edu.eduplatform.member.dto.PasswordResetRequest;
import com.edu.eduplatform.member.service.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PasswordResetViewController {

    private final MemberService memberService;

    @GetMapping("/password-reset")
    public String requestForm(@RequestParam(required = false) String requested, Model model) {
        model.addAttribute("requested", requested != null);
        return "member/password-reset-request";
    }

    @PostMapping("/password-reset")
    public String requestReset(@Valid PasswordResetRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", validationMessages(bindingResult));
            return "member/password-reset-request";
        }
        memberService.requestPasswordReset(request.email());
        return "redirect:/password-reset?requested";
    }

    @GetMapping("/password-reset/confirm")
    public String confirmForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        if (!memberService.isValidResetToken(token)) {
            model.addAttribute("errorMessage", "유효하지 않거나 만료된 링크입니다. 다시 요청해 주세요.");
        }
        return "member/password-reset-confirm";
    }

    @PostMapping("/password-reset/confirm")
    public String confirmReset(@Valid PasswordResetConfirmRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", request.token());
            model.addAttribute("validationErrors", validationMessages(bindingResult));
            return "member/password-reset-confirm";
        }
        if (!memberService.resetPassword(request.token(), request.newPassword())) {
            model.addAttribute("token", request.token());
            model.addAttribute("errorMessage", "유효하지 않거나 만료된 링크입니다. 다시 요청해 주세요.");
            return "member/password-reset-confirm";
        }
        return "redirect:/login?resetSuccess";
    }

    private static List<String> validationMessages(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }
}
