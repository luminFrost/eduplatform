package com.edu.eduplatform.quiz.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class DailyQuizViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 비로그인이면_로그인으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/my/daily"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 로그인하면_오늘의_단어_페이지가_보인다() throws Exception {
        MvcResult signUp = mockMvc.perform(post("/members").with(csrf())
                        .param("email", "daily-test@example.com")
                        .param("nickname", "데일리테스터")
                        .param("memberType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("password", "password1234"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) signUp.getRequest().getSession();

        mockMvc.perform(get("/my/daily").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("오늘의 단어")));
    }
}
