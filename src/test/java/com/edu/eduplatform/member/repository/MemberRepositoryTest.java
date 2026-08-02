package com.edu.eduplatform.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberRole;
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
                .password("password1234")
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

    @Test
    void role을_지정하지_않으면_USER로_기본값이_저장된다() {
        Member member = Member.builder()
                .email("norole@example.com")
                .nickname("역할없음")
                .memberType(MemberType.ADULT)
                .level(EnglishLevel.BEGINNER)
                .password("password1234")
                .build();
        Member saved = memberRepository.save(member);

        Member found = memberRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getRole()).isEqualTo(MemberRole.USER);
    }
}
