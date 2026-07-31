package com.edu.eduplatform.member.controller;

import com.edu.eduplatform.common.web.CurrentMemberSession;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberCreateRequest;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.exception.DuplicateEmailException;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import com.edu.eduplatform.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberViewController {

    private final MemberService memberService;
    private final CurrentMemberSession currentMemberSession;

    @GetMapping("/new")
    public String signUpForm(Model model) {
        addFormOptions(model);
        return "member/signup-form";
    }

    @PostMapping
    public String signUp(@Valid MemberCreateRequest request, BindingResult bindingResult, Model model,
                          HttpServletRequest httpRequest) {
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
            currentMemberSession.set(httpRequest, response.id());
            return "redirect:/members/" + response.id();
        } catch (DuplicateEmailException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("form", request);
            addFormOptions(model);
            return "member/signup-form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("member", memberService.getMember(id));
            return "member/detail";
        } catch (MemberNotFoundException e) {
            return "redirect:/members/new";
        }
    }

    @PostMapping("/{id}/select")
    public String select(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            memberService.getMember(id);
        } catch (MemberNotFoundException e) {
            return "redirect:/members/new";
        }

        currentMemberSession.set(httpRequest, id);
        return "redirect:/courses";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("memberTypes", MemberType.values());
        model.addAttribute("levels", EnglishLevel.values());
    }
}
