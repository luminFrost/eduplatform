package com.edu.eduplatform.announcement.controller;

import com.edu.eduplatform.announcement.dto.AnnouncementRequest;
import com.edu.eduplatform.announcement.service.SiteAnnouncementService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** 관리자 전용 공지 배너 관리 화면 — SecurityConfig가 /admin/** 을 ROLE_ADMIN으로 이미 막아둔다. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/announcement")
public class AnnouncementAdminController {

    private final SiteAnnouncementService siteAnnouncementService;

    @GetMapping
    public String form(Model model) {
        model.addAttribute("currentMessage", siteAnnouncementService.getCurrentMessage().orElse(null));
        return "admin/announcement-form";
    }

    @PostMapping
    public String save(@Valid AnnouncementRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", validationMessages(bindingResult));
            model.addAttribute("currentMessage", request.message());
            return "admin/announcement-form";
        }
        siteAnnouncementService.save(request.message());
        return "redirect:/admin/announcement";
    }

    @PostMapping("/delete")
    public String delete() {
        siteAnnouncementService.clear();
        return "redirect:/admin/announcement";
    }

    private static List<String> validationMessages(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }
}
