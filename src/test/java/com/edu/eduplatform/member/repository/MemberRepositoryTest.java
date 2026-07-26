package com.edu.eduplatform.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberType;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void findByEmail_존재하는_이메일이면_회원을_반환한다() {
        Member member = Member.builder()
                .email("learner@example.com")
                .nickname("러너")
                .memberType(MemberType.ADULT)
                .level(EnglishLevel.BEGINNER)
                .build();
        memberRepository.save(member);

        Optional<Member> found = memberRepository.findByEmail("learner@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo("러너");
    }

    @Test
    void findByEmail_존재하지_않으면_빈값을_반환한다() {
        Optional<Member> found = memberRepository.findByEmail("nobody@example.com");

        assertThat(found).isEmpty();
    }
}
