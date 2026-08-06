package com.edu.eduplatform.member.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.member.domain.MemberRole;
import com.edu.eduplatform.member.dto.MemberAdminResponse;
import com.edu.eduplatform.member.exception.CannotChangeSelfRoleException;
import com.edu.eduplatform.member.exception.CannotForceWithdrawAdminException;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import com.edu.eduplatform.member.service.MemberService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

/** 관리자 전용 회원 관리 화면 — SecurityConfig가 /admin/** 을 ROLE_ADMIN으로 이미 막아둔다. */
@Controller
@RequiredArgsConstructor
public class MemberAdminController {

    private final MemberService memberService;

    @GetMapping("/admin/members")
    public String list(
            @CurrentMemberId Long currentMemberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String error,
            Model model
    ) {
        List<MemberAdminResponse> members = memberService.listMembers(keyword);
        model.addAttribute("members", members);
        model.addAttribute("currentMemberId", currentMemberId);
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("errorMessage", error);
        return "admin/member-list";
    }

    @PostMapping("/admin/members/{id}/role")
    public String changeRole(
            @PathVariable Long id,
            @CurrentMemberId Long currentMemberId,
            @RequestParam MemberRole role,
            @RequestParam(required = false) String keyword
    ) {
        UriComponentsBuilder redirect = UriComponentsBuilder.fromPath("/admin/members")
                .queryParamIfPresent("keyword", Optional.ofNullable(keyword));
        try {
            memberService.changeRole(id, currentMemberId, role);
        } catch (CannotChangeSelfRoleException | MemberNotFoundException e) {
            redirect.queryParam("error", e.getMessage());
        }
        return "redirect:" + redirect.build().encode().toUriString();
    }

    @PostMapping("/admin/members/{id}/withdraw")
    public String withdraw(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword
    ) {
        UriComponentsBuilder redirect = UriComponentsBuilder.fromPath("/admin/members")
                .queryParamIfPresent("keyword", Optional.ofNullable(keyword));
        try {
            memberService.withdrawByAdmin(id);
        } catch (CannotForceWithdrawAdminException | MemberNotFoundException e) {
            redirect.queryParam("error", e.getMessage());
        }
        return "redirect:" + redirect.build().encode().toUriString();
    }
}
