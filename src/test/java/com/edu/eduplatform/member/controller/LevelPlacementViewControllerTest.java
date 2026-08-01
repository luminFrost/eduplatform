package com.edu.eduplatform.member.controller;

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
class LevelPlacementViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 비로그인_상태에서도_대상_없이_접근하면_대상_선택_페이지가_보인다() throws Exception {
        mockMvc.perform(get("/members/new/level-test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("누구를 위한 가입인가요")));
    }

    @Test
    void 대상을_지정하면_문항_8개짜리_배치_테스트가_보인다() throws Exception {
        mockMvc.perform(get("/members/new/level-test").param("target", "ADULT"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("레벨 테스트")));
    }

    @Test
    void 제출하면_추천_레벨과_함께_가입_폼으로_리다이렉트된다() throws Exception {
        mockMvc.perform(post("/members/new/level-test").with(csrf())
                        .param("target", "ADULT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/members/new?target=ADULT&recommendedLevel=*"));
    }
}
