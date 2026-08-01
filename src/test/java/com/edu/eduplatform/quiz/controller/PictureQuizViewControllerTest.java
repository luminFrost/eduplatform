package com.edu.eduplatform.quiz.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PictureQuizViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 비로그인_상태에서도_그림_퀴즈에_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/quiz/picture"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("그림 퀴즈")));
    }

    @Test
    void 채점하면_결과_페이지로_리다이렉트된다() throws Exception {
        mockMvc.perform(post("/quiz/picture").with(csrf())
                        .param("answer-0", "아무값"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/quiz/picture/result?score=*"));
    }
}
