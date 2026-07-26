package com.edu.eduplatform.member.service;

import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.dto.MemberCreateRequest;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.member.exception.DuplicateEmailException;
import com.edu.eduplatform.member.exception.MemberNotFoundException;
import com.edu.eduplatform.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

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
                .build();

        return MemberResponse.from(memberRepository.save(member));
    }

    public MemberResponse getMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        return MemberResponse.from(member);
    }
}
