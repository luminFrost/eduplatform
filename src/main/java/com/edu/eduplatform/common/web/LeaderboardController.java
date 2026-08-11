package com.edu.eduplatform.common.web;

import com.edu.eduplatform.common.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 학습 리더보드 — SecurityConfig가 permitAll로 열어둬 비로그인도 볼 수 있다. */
@Controller
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/leaderboard")
    public String leaderboard(@CurrentMemberId Long memberId, Model model) {
        model.addAttribute("entries", leaderboardService.getTopEntries(memberId));
        return "leaderboard";
    }
}
