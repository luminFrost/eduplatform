package com.edu.eduplatform.course.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.course.repository.CourseReviewRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.progress.domain.LearningProgress;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
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
class CourseViewControllerTest {

    private static final String RAW_PASSWORD = "password1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseReviewRepository courseReviewRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LearningProgressRepository learningProgressRepository;

    @Test
    void 코스목록_잘못된_target값이면_500대신_필터없이_보여준다() throws Exception {
        mockMvc.perform(get("/courses").param("target", "BOGUS"))
                .andExpect(status().isOk());
    }

    @Test
    void 코스목록_잘못된_level값이면_500대신_필터없이_보여준다() throws Exception {
        mockMvc.perform(get("/courses").param("level", "BOGUS"))
                .andExpect(status().isOk());
    }

    @Test
    void 코스목록_sort파라미터가_RATING이면_200을_반환한다() throws Exception {
        mockMvc.perform(get("/courses").param("sort", "RATING"))
                .andExpect(status().isOk());
    }

    @Test
    void 코스목록_sort파라미터가_BOOKMARKS이면_200을_반환한다() throws Exception {
        mockMvc.perform(get("/courses").param("sort", "BOOKMARKS"))
                .andExpect(status().isOk());
    }

    @Test
    void 코스목록_잘못된_sort값이면_500대신_기본_정렬로_보여준다() throws Exception {
        mockMvc.perform(get("/courses").param("sort", "BOGUS"))
                .andExpect(status().isOk());
    }

    @Test
    void 코스상세_잘못된_type값이면_500대신_전체_레슨을_보여준다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("뷰컨트롤러테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(get("/courses/{id}", course.getId()).param("type", "BOGUS"))
                .andExpect(status().isOk());
    }

    @Test
    void 코스상세_비로그인이면_두번째_레슨부터_잠금_배지가_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("배지테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(2).title("2과")
                .content("내용").lessonType(LessonType.VOCAB).build());

        mockMvc.perform(get("/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("🔒 회원 전용")));
    }

    @Test
    void 코스상세_학습자_수가_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("학습자수테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(get("/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("이 코스를 시작한 학습자")));
    }

    @Test
    void 개인_코스_상세에는_학습자_수가_안_보인다() throws Exception {
        Course personalCourse = courseRepository.save(Course.builder()
                .title("개인코스테스트").description("설명").ownerId(1L)
                .focusAreas(java.util.Set.of(com.edu.eduplatform.lesson.domain.LessonType.VOCAB))
                .criteriaSource(com.edu.eduplatform.course.domain.CourseCriteriaSource.SELF_SELECTED)
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(get("/courses/{id}", personalCourse.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("이 코스를 시작한 학습자"))));
    }

    @Test
    void 코스목록_학습자가_있는_코스에_배지가_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("목록학습자수테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());
        Member member = memberRepository.save(Member.builder()
                .email("list-learner-test@example.com").nickname("목록학습자")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).build());
        LearningProgress progress = LearningProgress.builder().memberId(member.getId()).lessonId(lesson.getId()).build();
        progress.complete();
        learningProgressRepository.save(progress);

        mockMvc.perform(get("/courses").param("target", "ADULT").param("level", "BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("👥 1")));
    }

    @Test
    void 방금_등록한_코스는_목록과_상세에_NEW_배지가_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("신규코스배지테스트").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(get("/courses").param("target", "ADULT").param("level", "BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("🆕 NEW")));

        mockMvc.perform(get("/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("🆕 NEW")));
    }

    @Test
    void 레슨_7개짜리_코스는_목록과_상세에_예상_학습_시간_배지가_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("예상시간테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        for (int i = 1; i <= 7; i++) {
            lessonRepository.save(Lesson.builder()
                    .courseId(course.getId()).orderNo(i).title(i + "과")
                    .content("내용").lessonType(LessonType.VOCAB).build());
        }

        mockMvc.perform(get("/courses").param("target", "ADULT").param("level", "BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("⏱️ 약 35분")));

        mockMvc.perform(get("/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("⏱️ 약 35분")));
    }

    @Test
    void 레슨이_없는_코스는_예상_학습_시간_배지가_안_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("레슨없는코스테스트").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(get("/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("⏱️"))));
    }

    @Test
    void 로그인_회원은_코스를_즐겨찾기하고_다시_토글하면_해제할_수_있다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("북마크테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        MockHttpSession session = loginAs("bookmark-test@example.com", "북마크테스터");

        mockMvc.perform(post("/courses/{id}/bookmark", course.getId()).session(session).with(csrf()))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        assertThat(mockMvc.perform(get("/courses/{id}", course.getId()).session(session))
                        .andReturn().getResponse().getContentAsString())
                .contains("★ 즐겨찾기 해제");

        mockMvc.perform(post("/courses/{id}/bookmark", course.getId()).session(session).with(csrf()))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        assertThat(mockMvc.perform(get("/courses/{id}", course.getId()).session(session))
                        .andReturn().getResponse().getContentAsString())
                .contains("☆ 즐겨찾기");
    }

    @Test
    void 비로그인이면_즐겨찾기_토글은_로그인으로_리다이렉트된다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("북마크비로그인테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(post("/courses/{id}/bookmark", course.getId()).with(csrf()))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 로그인_회원은_리뷰를_작성하고_상세_페이지에서_확인할_수_있다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("리뷰테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        MockHttpSession session = loginAs("review-test@example.com", "리뷰테스터");

        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(session).with(csrf())
                        .param("rating", "5")
                        .param("comment", "정말 좋아요"))
                .andExpect(redirectedUrl("/courses/" + course.getId()));

        mockMvc.perform(get("/courses/{id}", course.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("정말 좋아요")))
                .andExpect(content().string(containsString("리뷰테스터")));
    }

    @Test
    void 같은_회원이_다시_작성하면_리뷰가_하나로_유지된다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("리뷰재작성테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        MockHttpSession session = loginAs("review-upsert-test@example.com", "리뷰업서트테스터");

        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(session).with(csrf())
                        .param("rating", "2")
                        .param("comment", "별로예요"))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(session).with(csrf())
                        .param("rating", "5")
                        .param("comment", "다시 보니 좋아요"))
                .andExpect(redirectedUrl("/courses/" + course.getId()));

        String content = mockMvc.perform(get("/courses/{id}", course.getId()).session(session))
                .andReturn().getResponse().getContentAsString();
        assertThat(content).contains("다시 보니 좋아요");
        assertThat(content).doesNotContain("별로예요");
    }

    @Test
    void 리뷰를_삭제할_수_있다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("리뷰삭제테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        MockHttpSession session = loginAs("review-delete-test@example.com", "리뷰삭제테스터");

        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(session).with(csrf())
                        .param("rating", "4")
                        .param("comment", "삭제될 리뷰"))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        mockMvc.perform(post("/courses/{id}/reviews/delete", course.getId()).session(session).with(csrf()))
                .andExpect(redirectedUrl("/courses/" + course.getId()));

        mockMvc.perform(get("/courses/{id}", course.getId()).session(session))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("삭제될 리뷰"))));
    }

    @Test
    void 비로그인이면_리뷰_작성은_로그인으로_리다이렉트된다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("리뷰비로그인테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).with(csrf())
                        .param("rating", "5"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 별점이_범위_밖이면_reviewError로_리다이렉트된다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("리뷰범위밖테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        MockHttpSession session = loginAs("review-invalid-test@example.com", "리뷰범위테스터");

        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(session).with(csrf())
                        .param("rating", "6"))
                .andExpect(redirectedUrl("/courses/" + course.getId() + "?reviewError"));
    }

    @Test
    void 로그인_회원은_리뷰에_도움돼요_투표를_토글할_수_있다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("도움돼요테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        MockHttpSession author = loginAs("helpful-author@example.com", "리뷰작성자");
        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(author).with(csrf())
                        .param("rating", "5")
                        .param("comment", "투표받을 리뷰"))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        Long reviewId = courseReviewRepository.findByCourseIdOrderByIdDesc(course.getId()).get(0).getId();
        MockHttpSession voter = loginAs("helpful-voter@example.com", "투표자");

        mockMvc.perform(post("/courses/{courseId}/reviews/{reviewId}/helpful", course.getId(), reviewId)
                        .session(voter).with(csrf()))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        mockMvc.perform(get("/courses/{id}", course.getId()).session(voter))
                .andExpect(content().string(containsString("도움돼요 취소 (1)")));

        mockMvc.perform(post("/courses/{courseId}/reviews/{reviewId}/helpful", course.getId(), reviewId)
                        .session(voter).with(csrf()))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        mockMvc.perform(get("/courses/{id}", course.getId()).session(voter))
                .andExpect(content().string(containsString("도움돼요 (0)")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("도움돼요 취소"))));
    }

    @Test
    void 비로그인이면_리뷰_목록에_투표버튼_없이_카운트만_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("비로그인투표테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        MockHttpSession author = loginAs("helpful-anon-author@example.com", "리뷰작성자2");
        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(author).with(csrf())
                        .param("rating", "4")
                        .param("comment", "비로그인 열람용 리뷰"))
                .andExpect(redirectedUrl("/courses/" + course.getId()));

        mockMvc.perform(get("/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("도움돼요 (0)")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("도움돼요 취소"))));
    }

    @Test
    void reviewSort파라미터가_HELPFUL이면_투표많은_리뷰가_먼저_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("도움순정렬테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        MockHttpSession author1 = loginAs("helpful-sort-author1@example.com", "먼저쓴사람");
        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(author1).with(csrf())
                        .param("rating", "3")
                        .param("comment", "투표없는리뷰"))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        MockHttpSession author2 = loginAs("helpful-sort-author2@example.com", "나중에쓴사람");
        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(author2).with(csrf())
                        .param("rating", "5")
                        .param("comment", "투표받은리뷰"))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        Long secondReviewId = courseReviewRepository.findByCourseIdOrderByIdDesc(course.getId()).get(0).getId();
        MockHttpSession voter = loginAs("helpful-sort-voter@example.com", "투표하는사람");
        mockMvc.perform(post("/courses/{courseId}/reviews/{reviewId}/helpful", course.getId(), secondReviewId)
                        .session(voter).with(csrf()))
                .andExpect(redirectedUrl("/courses/" + course.getId()));

        String content = mockMvc.perform(get("/courses/{id}", course.getId()).param("reviewSort", "HELPFUL"))
                .andReturn().getResponse().getContentAsString();
        assertThat(content.indexOf("투표받은리뷰")).isLessThan(content.indexOf("투표없는리뷰"));
    }

    @Test
    void 코스를_완료하면_수료증_페이지에_닉네임과_코스제목이_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("수료증테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());
        MockHttpSession session = loginAs("certificate-test@example.com", "수료증테스터");
        Member member = memberRepository.findByEmail("certificate-test@example.com").orElseThrow();
        LearningProgress progress = LearningProgress.builder().memberId(member.getId()).lessonId(lesson.getId()).build();
        progress.complete();
        learningProgressRepository.save(progress);

        mockMvc.perform(get("/courses/{id}/certificate", course.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("수료증테스터")))
                .andExpect(content().string(containsString("수료증테스트코스")));
    }

    @Test
    void 완료하지_않은_코스는_수료증_대신_코스상세로_리다이렉트된다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("미완료수료증테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        lessonRepository.save(Lesson.builder()
                .courseId(course.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());
        MockHttpSession session = loginAs("certificate-incomplete-test@example.com", "미완료테스터");

        mockMvc.perform(get("/courses/{id}/certificate", course.getId()).session(session))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
    }

    @Test
    void 개인_코스는_수료증_대신_코스상세로_리다이렉트된다() throws Exception {
        MockHttpSession session = loginAs("certificate-personal-test@example.com", "개인코스테스터");
        Member member = memberRepository.findByEmail("certificate-personal-test@example.com").orElseThrow();
        Course personalCourse = courseRepository.save(Course.builder()
                .title("개인수료증테스트코스").description("설명").ownerId(member.getId())
                .focusAreas(java.util.Set.of(LessonType.VOCAB))
                .criteriaSource(com.edu.eduplatform.course.domain.CourseCriteriaSource.SELF_SELECTED)
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(get("/courses/{id}/certificate", personalCourse.getId()).session(session))
                .andExpect(redirectedUrl("/courses/" + personalCourse.getId()));
    }

    @Test
    void 존재하지_않는_코스의_수료증은_코스목록으로_리다이렉트된다() throws Exception {
        MockHttpSession session = loginAs("certificate-notfound-test@example.com", "없는코스테스터");

        mockMvc.perform(get("/courses/{id}/certificate", 999999L).session(session))
                .andExpect(redirectedUrl("/courses"));
    }

    @Test
    void 비로그인이면_수료증_페이지는_로그인으로_리다이렉트된다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("비로그인수료증테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        mockMvc.perform(get("/courses/{id}/certificate", course.getId()))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 로그인_회원은_리뷰를_신고할_수_있고_배너가_보인다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("신고테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        MockHttpSession author = loginAs("report-author@example.com", "신고대상작성자");
        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(author).with(csrf())
                        .param("rating", "1")
                        .param("comment", "신고받을 리뷰"))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        Long reviewId = courseReviewRepository.findByCourseIdOrderByIdDesc(course.getId()).get(0).getId();
        MockHttpSession reporter = loginAs("report-reporter@example.com", "신고자");

        mockMvc.perform(post("/courses/{courseId}/reviews/{reviewId}/report", course.getId(), reviewId)
                        .session(reporter).with(csrf()))
                .andExpect(redirectedUrl("/courses/" + course.getId() + "?reported"));
        mockMvc.perform(get("/courses/{id}", course.getId()).param("reported", "").session(reporter))
                .andExpect(content().string(containsString("신고가 접수되었습니다")));
    }

    @Test
    void 비로그인이면_리뷰_신고는_로그인으로_리다이렉트된다() throws Exception {
        Course course = courseRepository.save(Course.builder()
                .title("비로그인신고테스트코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        MockHttpSession author = loginAs("report-anon-author@example.com", "신고대상작성자2");
        mockMvc.perform(post("/courses/{id}/reviews", course.getId()).session(author).with(csrf())
                        .param("rating", "2")
                        .param("comment", "신고받을 리뷰2"))
                .andExpect(redirectedUrl("/courses/" + course.getId()));
        Long reviewId = courseReviewRepository.findByCourseIdOrderByIdDesc(course.getId()).get(0).getId();

        mockMvc.perform(post("/courses/{courseId}/reviews/{reviewId}/report", course.getId(), reviewId).with(csrf()))
                .andExpect(redirectedUrl("/login"));
    }

    private MockHttpSession loginAs(String email, String nickname) throws Exception {
        memberRepository.save(Member.builder()
                .email(email).nickname(nickname)
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password(passwordEncoder.encode(RAW_PASSWORD)).build());

        MvcResult loginResult = mockMvc.perform(post("/login").with(csrf())
                        .param("email", email)
                        .param("password", RAW_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) loginResult.getRequest().getSession();
    }
}
