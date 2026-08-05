package com.edu.eduplatform.progress.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.progress.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 헤더 프래그먼트(fragments/layout.html)가 모든 페이지에서 공유되므로, "마이페이지" 링크에
 * 오늘 복습할 레슨 수를 배지로 보여주기 위해 모든 요청에 dueReviewCount를 미리 채워둔다.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ReviewBadgeControllerAdvice {

    private final ProgressService progressService;

    @ModelAttribute("dueReviewCount")
    public long dueReviewCount(@CurrentMemberId Long memberId) {
        return memberId != null ? progressService.countLessonsDueForReview(memberId) : 0;
    }
}
