package com.edu.eduplatform.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberRole;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.dto.MemberCreateRequest;
import com.edu.eduplatform.member.dto.MemberAdminResponse;
import com.edu.eduplatform.member.dto.MemberUpdateRequest;
import com.edu.eduplatform.member.dto.PasswordChangeRequest;
import com.edu.eduplatform.member.exception.CannotChangeSelfRoleException;
import com.edu.eduplatform.member.exception.CannotWithdrawAdminException;
import com.edu.eduplatform.member.exception.DuplicateEmailException;
import com.edu.eduplatform.member.exception.InvalidPasswordException;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.security.EmailVerificationService;
import com.edu.eduplatform.member.security.PasswordResetTokenService;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenService passwordResetTokenService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LearningProgressRepository learningProgressRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void signUp_동시가입으로_유니크_제약을_위반하면_중복이메일_예외로_변환한다() {
        MemberCreateRequest request = new MemberCreateRequest(
                "race@example.com", "레이스테스터", MemberType.ADULT, EnglishLevel.BEGINNER, "password1234");
        // 첫 조회 시점엔 중복이 없어 보이지만(레이스), 실제 저장 시점엔 이미 다른 요청이 먼저 커밋한 상황을 재현.
        when(memberRepository.findByEmail("race@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password1234")).thenReturn("hashed");
        when(memberRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("unique constraint violated"));

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void updateProfile_닉네임과_레벨을_변경한다() throws Exception {
        Member member = withId(Member.builder()
                .email("a@example.com").nickname("기존닉네임")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").build(), 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        memberService.updateProfile(1L, new MemberUpdateRequest("새닉네임", EnglishLevel.INTERMEDIATE));

        assertThat(member.getNickname()).isEqualTo("새닉네임");
        assertThat(member.getLevel()).isEqualTo(EnglishLevel.INTERMEDIATE);
    }

    @Test
    void updateProfile_존재하지_않는_회원이면_예외를_던진다() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.updateProfile(999L, new MemberUpdateRequest("닉네임", EnglishLevel.BEGINNER)))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void changePassword_현재_비밀번호가_맞으면_새_비밀번호로_해시해_저장한다() throws Exception {
        Member member = withId(Member.builder()
                .email("a@example.com").nickname("테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("old-hashed").build(), 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("oldPassword1", "old-hashed")).thenReturn(true);
        when(passwordEncoder.encode("newPassword1")).thenReturn("new-hashed");

        memberService.changePassword(1L, new PasswordChangeRequest("oldPassword1", "newPassword1"));

        assertThat(member.getPassword()).isEqualTo("new-hashed");
    }

    @Test
    void changePassword_현재_비밀번호가_틀리면_예외를_던지고_바꾸지_않는다() throws Exception {
        Member member = withId(Member.builder()
                .email("a@example.com").nickname("테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("old-hashed").build(), 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrongPassword", "old-hashed")).thenReturn(false);

        assertThatThrownBy(() -> memberService.changePassword(1L, new PasswordChangeRequest("wrongPassword", "newPassword1")))
                .isInstanceOf(InvalidPasswordException.class);
        assertThat(member.getPassword()).isEqualTo("old-hashed");
    }

    @Test
    void changePassword_존재하지_않는_회원이면_예외를_던진다() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.changePassword(999L, new PasswordChangeRequest("a", "newPassword1")))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void requestPasswordReset_존재하는_이메일이면_토큰을_발급한다() throws Exception {
        Member member = withId(Member.builder()
                .email("a@example.com").nickname("테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").build(), 1L);
        when(memberRepository.findByEmail("a@example.com")).thenReturn(Optional.of(member));

        memberService.requestPasswordReset("a@example.com");

        verify(passwordResetTokenService).issueToken(1L);
    }

    @Test
    void requestPasswordReset_존재하지_않는_이메일이면_조용히_무시한다() {
        when(memberRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        memberService.requestPasswordReset("nobody@example.com");

        verify(passwordResetTokenService, never()).issueToken(any());
    }

    @Test
    void isValidResetToken_유효한_토큰이면_true를_반환한다() {
        when(passwordResetTokenService.peek("valid-token")).thenReturn(Optional.of(1L));

        assertThat(memberService.isValidResetToken("valid-token")).isTrue();
    }

    @Test
    void isValidResetToken_무효한_토큰이면_false를_반환한다() {
        when(passwordResetTokenService.peek("invalid-token")).thenReturn(Optional.empty());

        assertThat(memberService.isValidResetToken("invalid-token")).isFalse();
    }

    @Test
    void resetPassword_유효한_토큰이면_비밀번호를_바꾸고_true를_반환한다() throws Exception {
        Member member = withId(Member.builder()
                .email("a@example.com").nickname("테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("old-hashed").build(), 1L);
        when(passwordResetTokenService.consume("valid-token")).thenReturn(Optional.of(1L));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("newPassword1")).thenReturn("new-hashed");

        boolean result = memberService.resetPassword("valid-token", "newPassword1");

        assertThat(result).isTrue();
        assertThat(member.getPassword()).isEqualTo("new-hashed");
    }

    @Test
    void resetPassword_무효하거나_만료된_토큰이면_false를_반환하고_바꾸지_않는다() {
        when(passwordResetTokenService.consume("invalid-token")).thenReturn(Optional.empty());

        boolean result = memberService.resetPassword("invalid-token", "newPassword1");

        assertThat(result).isFalse();
        verify(memberRepository, never()).save(any());
    }

    @Test
    void ensureEmailAvailable_이미_가입된_이메일이면_예외를_던진다() throws Exception {
        Member member = withId(Member.builder()
                .email("taken@example.com").nickname("테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").build(), 1L);
        when(memberRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.ensureEmailAvailable("taken@example.com"))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void ensureEmailAvailable_가입되지_않은_이메일이면_예외를_던지지_않는다() {
        when(memberRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        memberService.ensureEmailAvailable("new@example.com");
    }

    @Test
    void requestSignupVerification_인증번호를_발급한다() {
        when(emailVerificationService.issueCode("new@example.com")).thenReturn("123456");

        memberService.requestSignupVerification("new@example.com");

        verify(emailVerificationService).issueCode("new@example.com");
    }

    @Test
    void verifySignupCode_인증서비스에_위임한다() {
        when(emailVerificationService.verify("new@example.com", "123456")).thenReturn(true);

        assertThat(memberService.verifySignupCode("new@example.com", "123456")).isTrue();
    }

    @Test
    void withdraw_비밀번호가_맞으면_회원과_진행기록과_개인코스를_모두_삭제한다() throws Exception {
        Member member = withId(Member.builder()
                .email("withdraw@example.com").nickname("탈퇴테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").role(MemberRole.USER).build(), 1L);
        Course personalCourse = withCourseId(Course.builder()
                .title("개인코스").description("설명").ownerId(1L)
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build(), 100L);
        Lesson personalLesson = Lesson.builder()
                .courseId(100L).orderNo(1).title("1과").content("내용").lessonType(LessonType.VOCAB).build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password1234", "hashed")).thenReturn(true);
        when(courseRepository.findByOwnerIdOrderByIdDesc(1L)).thenReturn(List.of(personalCourse));
        when(lessonRepository.findByCourseIdIn(List.of(100L))).thenReturn(List.of(personalLesson));
        when(learningProgressRepository.findByMemberId(1L)).thenReturn(List.of());

        memberService.withdraw(1L, "password1234");

        verify(lessonRepository).deleteAll(List.of(personalLesson));
        verify(courseRepository).deleteAll(List.of(personalCourse));
        verify(learningProgressRepository).deleteAll(List.of());
        verify(memberRepository).delete(member);
    }

    @Test
    void withdraw_개인코스가_없어도_정상_탈퇴한다() throws Exception {
        Member member = withId(Member.builder()
                .email("withdraw-nopersonal@example.com").nickname("탈퇴테스터2")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").role(MemberRole.USER).build(), 2L);
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password1234", "hashed")).thenReturn(true);
        when(courseRepository.findByOwnerIdOrderByIdDesc(2L)).thenReturn(List.of());
        when(learningProgressRepository.findByMemberId(2L)).thenReturn(List.of());

        memberService.withdraw(2L, "password1234");

        verify(lessonRepository, never()).deleteAll(any());
        verify(memberRepository).delete(member);
    }

    @Test
    void withdraw_비밀번호가_틀리면_예외를_던지고_아무것도_지우지_않는다() throws Exception {
        Member member = withId(Member.builder()
                .email("withdraw-wrong@example.com").nickname("탈퇴테스터3")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").role(MemberRole.USER).build(), 3L);
        when(memberRepository.findById(3L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> memberService.withdraw(3L, "wrong-password"))
                .isInstanceOf(InvalidPasswordException.class);
        verify(memberRepository, never()).delete(any());
    }

    @Test
    void withdraw_관리자면_예외를_던지고_아무것도_지우지_않는다() throws Exception {
        Member admin = withId(Member.builder()
                .email("withdraw-admin@example.com").nickname("관리자")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").role(MemberRole.ADMIN).build(), 4L);
        when(memberRepository.findById(4L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> memberService.withdraw(4L, "password1234"))
                .isInstanceOf(CannotWithdrawAdminException.class);
        verify(memberRepository, never()).delete(any());
    }

    @Test
    void listMembers_키워드가_없으면_전체를_id_오름차순으로_반환한다() throws Exception {
        Member m2 = withId(Member.builder()
                .email("b@example.com").nickname("두번째")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER).password("hashed").build(), 2L);
        Member m1 = withId(Member.builder()
                .email("a@example.com").nickname("첫번째")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER).password("hashed").build(), 1L);
        when(memberRepository.findAll()).thenReturn(List.of(m2, m1));

        List<MemberAdminResponse> result = memberService.listMembers(null);

        assertThat(result).extracting(MemberAdminResponse::id).containsExactly(1L, 2L);
    }

    @Test
    void listMembers_키워드가_있으면_이메일_닉네임으로_검색한다() {
        when(memberRepository.findByEmailContainingIgnoreCaseOrNicknameContainingIgnoreCase("루나", "루나"))
                .thenReturn(List.of());

        memberService.listMembers("루나");

        org.mockito.Mockito.verify(memberRepository)
                .findByEmailContainingIgnoreCaseOrNicknameContainingIgnoreCase("루나", "루나");
    }

    @Test
    void changeRole_정상적으로_역할을_바꾼다() throws Exception {
        Member member = withId(Member.builder()
                .email("a@example.com").nickname("테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER).password("hashed").build(), 2L);
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));

        memberService.changeRole(2L, 1L, MemberRole.ADMIN);

        assertThat(member.getRole()).isEqualTo(MemberRole.ADMIN);
    }

    @Test
    void changeRole_자기_자신이면_예외를_던진다() {
        assertThatThrownBy(() -> memberService.changeRole(1L, 1L, MemberRole.ADMIN))
                .isInstanceOf(CannotChangeSelfRoleException.class);
    }

    @Test
    void changeRole_존재하지_않는_회원이면_예외를_던진다() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.changeRole(999L, 1L, MemberRole.ADMIN))
                .isInstanceOf(MemberNotFoundException.class);
    }

    private static Member withId(Member member, Long id) throws Exception {
        Field field = member.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(member, id);
        return member;
    }

    private static Course withCourseId(Course course, Long id) throws Exception {
        Field field = course.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(course, id);
        return course;
    }
}
