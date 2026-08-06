package com.edu.eduplatform.member.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberRole;
import com.edu.eduplatform.member.dto.MemberAdminResponse;
import com.edu.eduplatform.member.dto.MemberCreateRequest;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.dto.MemberUpdateRequest;
import com.edu.eduplatform.member.dto.PasswordChangeRequest;
import com.edu.eduplatform.member.exception.CannotChangeSelfRoleException;
import com.edu.eduplatform.member.exception.CannotForceWithdrawAdminException;
import com.edu.eduplatform.member.exception.CannotWithdrawAdminException;
import com.edu.eduplatform.member.exception.DuplicateEmailException;
import com.edu.eduplatform.member.exception.InvalidPasswordException;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.member.security.EmailVerificationService;
import com.edu.eduplatform.member.security.PasswordResetTokenService;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailVerificationService emailVerificationService;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LearningProgressRepository learningProgressRepository;

    @Transactional
    public MemberResponse signUp(MemberCreateRequest request) {
        memberRepository.findByEmail(request.email()).ifPresent(member -> {
            throw new DuplicateEmailException(request.email());
        });

        Member member = Member.builder()
                .email(request.email())
                .nickname(request.nickname())
                .memberType(request.memberType())
                .level(request.level())
                .password(passwordEncoder.encode(request.password()))
                .build();

        try {
            // saveAndFlush: 동시 가입 레이스로 unique 제약을 위반하면 이 안에서 바로 터지게 해서 잡아낸다.
            return MemberResponse.from(memberRepository.saveAndFlush(member));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException(request.email());
        }
    }

    /** 인증번호 발급 전에 이미 가입된 이메일인지 미리 확인 — 헛되이 코드를 발급하지 않는다. */
    public void ensureEmailAvailable(String email) {
        memberRepository.findByEmail(email).ifPresent(member -> {
            throw new DuplicateEmailException(email);
        });
    }

    /**
     * 회원가입 인증번호를 발급해 로그에 출력한다(실제 SMTP 미연동 — 로그가 메일 발송 자리를 대신함,
     * 비밀번호 재설정과 같은 패턴).
     */
    public void requestSignupVerification(String email) {
        String code = emailVerificationService.issueCode(email);
        log.info("[회원가입 인증] {} 요청 — 인증번호: {}", email, code);
    }

    public boolean verifySignupCode(String email, String code) {
        return emailVerificationService.verify(email, code);
    }

    public MemberResponse getMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        member.changeNickname(request.nickname());
        member.changeLevel(request.level());
        member.changeWeeklyGoal(request.weeklyGoal());
        memberRepository.save(member);

        return MemberResponse.from(member);
    }

    /** 세션의 인증 정보는 로그인 시점 스냅샷이라, 비밀번호를 바꿔도 현재 세션엔 영향이 없다(다음 로그인부터 적용). */
    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new InvalidPasswordException();
        }

        member.changePassword(passwordEncoder.encode(request.newPassword()));
        memberRepository.save(member);
    }

    /**
     * 회원 스스로 계정을 삭제한다 — 진행 기록과 개인 코스(전용으로 복사된 레슨 포함)를 함께 지운다.
     * 개인 코스는 다른 회원과 공유되지 않아(레슨도 복사본) 지워도 다른 회원에게 영향이 없다.
     * 관리자 계정은 잠금 사고 방지를 위해 자기 자신을 탈퇴시킬 수 없다.
     */
    @Transactional
    public void withdraw(Long memberId, String password) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        if (member.getRole() == MemberRole.ADMIN) {
            throw new CannotWithdrawAdminException();
        }
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new InvalidPasswordException();
        }

        deleteMemberAndData(member);
    }

    /**
     * 관리자가 다른 회원을 강제로 탈퇴시킨다 — 본인 확인(비밀번호) 없이 관리자 권한만으로 실행한다.
     * 대상이 관리자 계정이면(자기 자신도 ADMIN 역할이라 여기서 함께 걸러진다) 잠금 사고 방지를 위해 거부한다.
     */
    @Transactional
    public void withdrawByAdmin(Long targetMemberId) {
        Member member = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new MemberNotFoundException(targetMemberId));
        if (member.getRole() == MemberRole.ADMIN) {
            throw new CannotForceWithdrawAdminException();
        }

        deleteMemberAndData(member);
    }

    /** 회원과 진행 기록·개인 코스(전용으로 복사된 레슨 포함)를 함께 지운다. */
    private void deleteMemberAndData(Member member) {
        Long memberId = member.getId();
        List<Course> personalCourses = courseRepository.findByOwnerIdOrderByIdDesc(memberId);
        if (!personalCourses.isEmpty()) {
            List<Long> personalCourseIds = personalCourses.stream().map(Course::getId).toList();
            lessonRepository.deleteAll(lessonRepository.findByCourseIdIn(personalCourseIds));
            courseRepository.deleteAll(personalCourses);
        }
        learningProgressRepository.deleteAll(learningProgressRepository.findByMemberId(memberId));
        memberRepository.delete(member);
    }

    /**
     * 이메일이 존재하면 재설정 토큰을 발급해 로그에 링크를 출력한다(실제 SMTP 미연동 — 로그가 메일
     * 발송 자리를 대신함). 존재하지 않는 이메일이어도 조용히 반환 — 이메일 존재 여부를 노출하지
     * 않는다(로그인 시도 제한과 같은 원칙).
     */
    @Transactional
    public void requestPasswordReset(String email) {
        memberRepository.findByEmail(email).ifPresent(member -> {
            String token = passwordResetTokenService.issueToken(member.getId());
            log.info("[비밀번호 재설정] {} 요청 — 링크: /password-reset/confirm?token={}", email, token);
        });
    }

    public boolean isValidResetToken(String token) {
        return passwordResetTokenService.peek(token).isPresent();
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<Long> memberId = passwordResetTokenService.consume(token);
        if (memberId.isEmpty()) {
            return false;
        }
        Member member = memberRepository.findById(memberId.get())
                .orElseThrow(() -> new MemberNotFoundException(memberId.get()));
        member.changePassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
        return true;
    }

    /** 관리자 회원 목록 — 키워드가 있으면 이메일·닉네임 부분 일치 검색, 없으면 전체를 id 오름차순으로 반환한다. */
    public List<MemberAdminResponse> listMembers(String keyword) {
        List<Member> members = StringUtils.hasText(keyword)
                ? memberRepository.findByEmailContainingIgnoreCaseOrNicknameContainingIgnoreCase(keyword, keyword)
                : memberRepository.findAll();
        return members.stream()
                .sorted(Comparator.comparing(Member::getId))
                .map(MemberAdminResponse::from)
                .toList();
    }

    /** 관리자가 다른 회원의 역할을 바꾼다. 자기 자신은 대상이 될 수 없다(잠금 방지). */
    @Transactional
    public void changeRole(Long targetMemberId, Long actingMemberId, MemberRole newRole) {
        if (targetMemberId.equals(actingMemberId)) {
            throw new CannotChangeSelfRoleException();
        }
        Member member = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new MemberNotFoundException(targetMemberId));
        member.changeRole(newRole);
        memberRepository.save(member);
    }
}
