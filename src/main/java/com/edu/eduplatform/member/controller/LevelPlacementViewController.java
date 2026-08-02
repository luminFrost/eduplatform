package com.edu.eduplatform.member.controller;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.service.QuestionService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 가입 전 "레벨을 모르겠어요" 회원이 쓰는 짧은 레벨 배치 테스트. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/members/new/level-test")
public class LevelPlacementViewController {

    private final QuestionService questionService;

    @GetMapping
    public String form(@RequestParam(required = false) MemberType target, Model model) {
        if (target == null) {
            return "member/level-test-target";
        }
        model.addAttribute("target", target);
        model.addAttribute("questions", questionService.getLevelPlacementQuestions(target));
        return "member/level-test";
    }

    @PostMapping
    public String submit(@RequestParam MemberType target, @RequestParam Map<String, String> params) {
        EnglishLevel recommended = questionService.recommendLevel(target, parseAnswers(params));
        return "redirect:/members/new?target=" + target + "&recommendedLevel=" + recommended;
    }

    /** 라디오 그룹 name="answer-{questionId}" 로 제출된 값들을 questionId → 선택한 보기 인덱스로 모은다. */
    private static Map<Long, Integer> parseAnswers(Map<String, String> params) {
        Map<Long, Integer> answers = new HashMap<>();
        params.forEach((key, value) -> {
            if (key.startsWith("answer-")) {
                answers.put(Long.valueOf(key.substring("answer-".length())), Integer.valueOf(value));
            }
        });
        return answers;
    }
}
