package com.edu.eduplatform.question.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberRole;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.question.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionAdminControllerTest {

    private static final String RAW_PASSWORD = "password1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void 비로그인이면_로그인으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/admin/questions"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 일반_회원은_접근하면_403이다() throws Exception {
        MockHttpSession session = loginAs("question-admin-member@example.com", "일반회원", MemberRole.USER);

        mockMvc.perform(get("/admin/questions").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_문항을_생성하고_수정하고_삭제할_수_있다() throws Exception {
        MockHttpSession session = loginAs("question-admin@example.com", "관리자", MemberRole.ADMIN);

        MvcResult createResult = mockMvc.perform(post("/admin/questions/new").session(session).with(csrf())
                        .param("targetType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("lessonType", "VOCAB")
                        .param("prompt", "새 문제입니다")
                        .param("option1", "보기1")
                        .param("option2", "보기2")
                        .param("option3", "보기3")
                        .param("option4", "보기4")
                        .param("correctOptionIndex", "1"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        assertThat(createResult.getResponse().getRedirectedUrl()).isEqualTo("/admin/questions");

        var created = questionRepository.findAll().stream()
                .filter(q -> "새 문제입니다".equals(q.getPrompt()))
                .findFirst()
                .orElseThrow();
        assertThat(created.getCorrectOptionIndex()).isEqualTo(1);

        // options는 지연 로딩 컬렉션이라 트랜잭션 밖에서 직접 접근할 수 없다 — 서비스 계층을 통해
        // (수정 폼 렌더링 경로로) 확인한다. 실제 서비스는 클래스 레벨 @Transactional 안에서 접근하므로 안전.
        mockMvc.perform(get("/admin/questions/" + created.getId() + "/edit").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("보기1")))
                .andExpect(content().string(containsString("보기4")));

        mockMvc.perform(post("/admin/questions/" + created.getId() + "/edit").session(session).with(csrf())
                        .param("targetType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("lessonType", "VOCAB")
                        .param("prompt", "수정된 문제입니다")
                        .param("option1", "새보기1")
                        .param("option2", "새보기2")
                        .param("option3", "새보기3")
                        .param("option4", "새보기4")
                        .param("correctOptionIndex", "2"))
                .andExpect(status().is3xxRedirection());

        var updated = questionRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getPrompt()).isEqualTo("수정된 문제입니다");
        assertThat(updated.getCorrectOptionIndex()).isEqualTo(2);

        mockMvc.perform(post("/admin/questions/" + created.getId() + "/delete").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(questionRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void 관리자는_문제를_비우면_검증_오류로_폼이_그대로_남는다() throws Exception {
        MockHttpSession session = loginAs("question-admin-invalid@example.com", "관리자2", MemberRole.ADMIN);

        mockMvc.perform(post("/admin/questions/new").session(session).with(csrf())
                        .param("targetType", "ADULT")
                        .param("level", "BEGINNER")
                        .param("lessonType", "VOCAB")
                        .param("prompt", "")
                        .param("option1", "보기1")
                        .param("option2", "보기2")
                        .param("option3", "보기3")
                        .param("option4", "보기4")
                        .param("correctOptionIndex", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("문제를 입력해 주세요")));
    }

    @Test
    void 일반_회원은_문항_일괄_등록에_접근하면_403이다() throws Exception {
        MockHttpSession session = loginAs("question-import-member@example.com", "일반회원", MemberRole.USER);

        mockMvc.perform(post("/admin/questions/import").session(session).with(csrf())
                        .param("json", "[]"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_JSON_배열로_문항_여러개를_한번에_등록할_수_있다() throws Exception {
        MockHttpSession session = loginAs("question-import-admin@example.com", "관리자", MemberRole.ADMIN);

        String json = """
                [
                  {
                    "targetType": "ADULT", "level": "BEGINNER", "lessonType": "VOCAB",
                    "prompt": "문제1", "option1": "a", "option2": "b", "option3": "c", "option4": "d",
                    "correctOptionIndex": 0
                  },
                  {
                    "targetType": "CHILD", "level": "ELEMENTARY", "lessonType": "READING",
                    "prompt": "문제2", "option1": "a", "option2": "b", "option3": "c", "option4": "d",
                    "correctOptionIndex": 3
                  }
                ]
                """;

        mockMvc.perform(post("/admin/questions/import").session(session).with(csrf())
                        .param("json", json))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/questions"));

        assertThat(questionRepository.findAll()).extracting(q -> q.getPrompt())
                .contains("문제1", "문제2");
    }

    @Test
    void 잘못된_JSON_형식이면_에러를_보여주고_아무것도_저장하지_않는다() throws Exception {
        MockHttpSession session = loginAs("question-import-malformed@example.com", "관리자", MemberRole.ADMIN);
        long before = questionRepository.count();

        mockMvc.perform(post("/admin/questions/import").session(session).with(csrf())
                        .param("json", "이건 JSON이 아니에요"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("JSON 형식을 확인해 주세요")));

        assertThat(questionRepository.count()).isEqualTo(before);
    }

    @Test
    void correctOptionIndex가_범위를_벗어나면_행_번호와_함께_에러를_보여준다() throws Exception {
        MockHttpSession session = loginAs("question-import-range@example.com", "관리자", MemberRole.ADMIN);
        long before = questionRepository.count();

        String json = """
                [
                  {
                    "targetType": "ADULT", "level": "BEGINNER", "lessonType": "VOCAB",
                    "prompt": "문제1", "option1": "a", "option2": "b", "option3": "c", "option4": "d",
                    "correctOptionIndex": 9
                  }
                ]
                """;

        mockMvc.perform(post("/admin/questions/import").session(session).with(csrf())
                        .param("json", json))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1번째 항목")));

        assertThat(questionRepository.count()).isEqualTo(before);
    }

    private MockHttpSession loginAs(String email, String nickname, MemberRole role) throws Exception {
        memberRepository.save(Member.builder()
                .email(email).nickname(nickname)
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).role(role).build());

        MvcResult loginResult = mockMvc.perform(post("/login").with(csrf())
                        .param("email", email)
                        .param("password", RAW_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();
        assertThat(session).isNotNull();
        return session;
    }
}
