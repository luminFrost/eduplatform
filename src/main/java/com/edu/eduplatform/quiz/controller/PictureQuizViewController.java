package com.edu.eduplatform.quiz.controller;

import com.edu.eduplatform.quiz.dto.PictureQuizResult;
import com.edu.eduplatform.quiz.service.PictureQuizService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/quiz/picture")
public class PictureQuizViewController {

    private final PictureQuizService pictureQuizService;

    @GetMapping
    public String form(Model model) {
        model.addAttribute("questions", pictureQuizService.getTodayQuestions());
        return "quiz/picture";
    }

    @PostMapping
    public String submit(@RequestParam Map<String, String> params) {
        List<String> answers = new ArrayList<>();
        for (int i = 0; params.containsKey("answer-" + i); i++) {
            answers.add(params.get("answer-" + i));
        }
        PictureQuizResult result = pictureQuizService.score(answers);
        return "redirect:/quiz/picture/result?score=" + result.correct() + "&total=" + result.total();
    }

    @GetMapping("/result")
    public String result(@RequestParam int score, @RequestParam int total, Model model) {
        model.addAttribute("score", score);
        model.addAttribute("total", total);
        return "quiz/picture-result";
    }
}
