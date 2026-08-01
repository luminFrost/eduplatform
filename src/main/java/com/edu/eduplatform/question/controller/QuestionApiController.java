package com.edu.eduplatform.question.controller;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.dto.QuestionResponse;
import com.edu.eduplatform.question.service.QuestionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionApiController {

    private final QuestionService questionService;

    @GetMapping("/diagnostic-test")
    public List<QuestionResponse> diagnosticTestQuestions(
            @RequestParam MemberType target, @RequestParam EnglishLevel level
    ) {
        return questionService.getDiagnosticTestQuestions(target, level);
    }
}
