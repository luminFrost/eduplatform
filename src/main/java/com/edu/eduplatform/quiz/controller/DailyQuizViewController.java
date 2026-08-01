package com.edu.eduplatform.quiz.controller;

import com.edu.eduplatform.common.web.CurrentMemberId;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.service.MemberService;
import com.edu.eduplatform.quiz.service.DailyWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my/daily")
public class DailyQuizViewController {

    private final DailyWordService dailyWordService;
    private final MemberService memberService;

    @GetMapping
    public String daily(@CurrentMemberId Long memberId,
                         @RequestParam(required = false) String quizResult, Model model) {
        MemberResponse member = memberService.getMember(memberId);
        model.addAttribute("words", dailyWordService.getTodayWords(member));
        model.addAttribute("quiz", dailyWordService.getTodayQuiz(member).orElse(null));
        model.addAttribute("quizResult", quizResult);
        return "my/daily";
    }

    @PostMapping("/quiz")
    public String submitQuiz(@CurrentMemberId Long memberId, @RequestParam String answer) {
        MemberResponse member = memberService.getMember(memberId);
        boolean correct = dailyWordService.checkAnswer(member, answer);
        return "redirect:/my/daily?quizResult=" + (correct ? "correct" : "wrong");
    }
}
