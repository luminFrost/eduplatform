package com.edu.eduplatform.member.service;

import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberRole;
import com.edu.eduplatform.member.dto.MemberAdminResponse;
import com.edu.eduplatform.member.dto.MemberCreateRequest;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.dto.MemberUpdateRequest;
import com.edu.eduplatform.member.dto.PasswordChangeRequest;
import com.edu.eduplatform.member.exception.CannotChangeSelfRoleException;
import com.edu.eduplatform.member.exception.DuplicateEmailException;
import com.edu.eduplatform.member.exception.InvalidPasswordException;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import com.edu.eduplatform.member.repository.MemberRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
