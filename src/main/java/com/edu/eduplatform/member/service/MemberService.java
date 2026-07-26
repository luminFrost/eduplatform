package com.edu.eduplatform.member.service;

import com.edu.eduplatform.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    // TODO: 회원 가입, 조회, 레벨 변경 등 비즈니스 로직 추가
}
