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
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberViewController {

    private final MemberService memberService;
    private final MemberUserDetailsService memberUserDetailsService;
    private final CurrentMemberSession currentMemberSession;

    @GetMapping("/new")
    public String signUpForm(Model model) {
        addFormOptions(model);
        return "member/signup-form";
    }

    @PostMapping
    public String signUp(@Valid MemberCreateRequest request, BindingResult bindingResult, Model model,
                          HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList());
            model.addAttribute("form", request);
            addFormOptions(model);
            return "member/signup-form";
        }

        try {
            MemberResponse response = memberService.signUp(request);
            UserDetails userDetails = memberUserDetailsService.loadUserByUsername(response.email());
            currentMemberSession.login(httpRequest, httpResponse, userDetails);
            return "redirect:/my";
        } catch (DuplicateEmailException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("form", request);
            addFormOptions(model);
            return "member/signup-form";
        }
    }

    private void addFormOptions(Model model) {
        model.addAttribute("memberTypes", MemberType.values());
        model.addAttribute("levels", EnglishLevel.values());
    }
}
