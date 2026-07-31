package com.edu.eduplatform.member.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.repository.MemberRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MemberApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 회원가입_성공하면_201과_회원정보를_반환한다() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new SignUpRequest(
                "new-learner@example.com", "새러너", MemberType.ADULT, EnglishLevel.BEGINNER, "password1234"));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new-learner@example.com"))
                .andExpect(jsonPath("$.nickname").value("새러너"));
    }

    @Test
    void 회원가입_이메일이_중복이면_409를_반환한다() throws Exception {
        memberRepository.save(Member.builder()
                .email("dup@example.com")
                .nickname("기존회원")
                .memberType(MemberType.CHILD)
                .level(EnglishLevel.BEGINNER)
                .password("password1234")
                .build());

        String requestBody = objectMapper.writeValueAsString(new SignUpRequest(
                "dup@example.com", "새회원", MemberType.CHILD, EnglishLevel.BEGINNER, "password1234"));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("dup@example.com")));
    }

    @Test
    void 회원가입_이메일_형식이_아니면_400을_반환한다() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new SignUpRequest(
                "not-an-email", "닉네임", MemberType.ADULT, EnglishLevel.BEGINNER, "password1234"));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 회원조회_존재하면_200과_회원정보를_반환한다() throws Exception {
        Member saved = memberRepository.save(Member.builder()
                .email("lookup@example.com")
                .nickname("조회대상")
                .memberType(MemberType.ADULT)
                .level(EnglishLevel.INTERMEDIATE)
                .password("password1234")
                .build());

        mockMvc.perform(get("/api/members/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("lookup@example.com"));
    }

    @Test
    void 회원조회_존재하지_않으면_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/members/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    private record SignUpRequest(String email, String nickname, MemberType memberType, EnglishLevel level, String password) {
    }
}
