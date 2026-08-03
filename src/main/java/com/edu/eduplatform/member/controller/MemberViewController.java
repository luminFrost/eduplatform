package com.edu.eduplatform.member.controller;

import com.edu.eduplatform.common.web.CurrentMemberSession;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberCreateRequest;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.exception.DuplicateEmailException;
import com.edu.eduplatform.member.security.MemberUserDetailsService;
import com.edu.eduplatform.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberViewController {

    private static final String PENDING_SIGNUP_KEY = "pendingSignup";

    private final MemberService memberService;
    private final MemberUserDetailsService memberUserDetailsService;
    private final CurrentMemberSession currentMemberSession;

    @GetMapping("/new")
    public String signUpForm(@RequestParam(required = false) String target,
                              @RequestParam(required = false) String recommendedLevel, Model model) {
        addFormOptions(model);
        model.addAttribute("recommendedTarget", target);
        model.addAttribute("recommendedLevel", recommendedLevel);
        return "member/signup-form";
    }

    @PostMapping("/new")
    public String requestVerification(@Valid MemberCreateRequest request, BindingResult bindingResult,
                                       Model model, HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", validationMessages(bindingResult));
            model.addAttribute("form", request);
            addFormOptions(model);
            return "member/signup-form";
        }

        try {
            memberService.ensureEmailAvailable(request.email());
        } catch (DuplicateEmailException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("form", request);
            addFormOptions(model);
            return "member/signup-form";
        }

        httpRequest.getSession().setAttribute(PENDING_SIGNUP_KEY, request);
        memberService.requestSignupVerification(request.email());
        return "redirect:/members/new/verify";
    }

    @GetMapping("/new/verify")
    public String verifyForm(HttpServletRequest httpRequest, Model model) {
        MemberCreateRequest pending = (MemberCreateRequest) httpRequest.getSession().getAttribute(PENDING_SIGNUP_KEY);
        if (pending == null) {
            return "redirect:/members/new";
        }
        model.addAttribute("email", pending.email());
        return "member/signup-verify";
    }

    @PostMapping("/new/verify")
    public String verify(@RequestParam String code, HttpServletRequest httpRequest,
                          HttpServletResponse httpResponse, Model model) {
        MemberCreateRequest pending = (MemberCreateRequest) httpRequest.getSession().getAttribute(PENDING_SIGNUP_KEY);
        if (pending == null) {
            return "redirect:/members/new";
        }

        if (!memberService.verifySignupCode(pending.email(), code)) {
            model.addAttribute("email", pending.email());
            model.addAttribute("errorMessage", "인증번호가 올바르지 않거나 만료됐습니다.");
            return "member/signup-verify";
        }

        httpRequest.getSession().removeAttribute(PENDING_SIGNUP_KEY);
        try {
            MemberResponse response = memberService.signUp(pending);
            UserDetails userDetails = memberUserDetailsService.loadUserByUsername(response.email());
            currentMemberSession.login(httpRequest, httpResponse, userDetails);
            return "redirect:/my";
        } catch (DuplicateEmailException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("form", pending);
            addFormOptions(model);
            return "member/signup-form";
        }
    }

    private void addFormOptions(Model model) {
        model.addAttribute("memberTypes", MemberType.values());
        model.addAttribute("levels", EnglishLevel.values());
    }

    private static List<String> validationMessages(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }
}
