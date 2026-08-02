package com.edu.eduplatform.member.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.dto.MemberUpdateRequest;
import com.edu.eduplatform.member.dto.PasswordChangeRequest;
import com.edu.eduplatform.member.exception.InvalidPasswordException;
import com.edu.eduplatform.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my/profile")
public class MemberProfileViewController {

    private final MemberService memberService;

    @GetMapping
    public String form(@CurrentMemberId Long memberId,
                        @RequestParam(required = false) String updated,
                        @RequestParam(required = false) String passwordChanged,
                        Model model) {
        model.addAttribute("member", memberService.getMember(memberId));
        model.addAttribute("levels", EnglishLevel.values());
        model.addAttribute("updated", updated != null);
        model.addAttribute("passwordChanged", passwordChanged != null);
        return "my/profile";
    }

    @PostMapping
    public String updateProfile(@CurrentMemberId Long memberId, @Valid MemberUpdateRequest request,
                                 BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return withErrors(memberId, model, bindingResult);
        }

        memberService.updateProfile(memberId, request);
        return "redirect:/my/profile?updated";
    }

    @PostMapping("/password")
    public String changePassword(@CurrentMemberId Long memberId, @Valid PasswordChangeRequest request,
                                  BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return withErrors(memberId, model, bindingResult);
        }

        try {
            memberService.changePassword(memberId, request);
        } catch (InvalidPasswordException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("member", memberService.getMember(memberId));
            model.addAttribute("levels", EnglishLevel.values());
            return "my/profile";
        }

        return "redirect:/my/profile?passwordChanged";
    }

    private String withErrors(Long memberId, Model model, BindingResult bindingResult) {
        model.addAttribute("validationErrors", bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList());
        model.addAttribute("member", memberService.getMember(memberId));
        model.addAttribute("levels", EnglishLevel.values());
        return "my/profile";
    }
}
