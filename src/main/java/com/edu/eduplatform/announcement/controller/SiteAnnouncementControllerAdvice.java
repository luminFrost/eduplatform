package com.edu.eduplatform.announcement.controller;

import com.edu.eduplatform.announcement.service.SiteAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 헤더 프래그먼트(fragments/layout.html)가 모든 페이지에서 공유되므로, 등록된 공지가 있으면
 * 모든 요청에 siteAnnouncementMessage를 미리 채워둔다.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class SiteAnnouncementControllerAdvice {

    private final SiteAnnouncementService siteAnnouncementService;

    @ModelAttribute("siteAnnouncementMessage")
    public String siteAnnouncementMessage() {
        return siteAnnouncementService.getCurrentMessage().orElse(null);
    }
}
