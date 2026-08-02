package com.edu.eduplatform.member.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class MemberUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private MemberUserDetailsService memberUserDetailsService;

    @Test
    void loadUserByUsername_존재하고_잠기지_않았으면_정상_반환한다() {
        Member member = Member.builder()
                .email("a@example.com").nickname("테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").build();
        when(memberRepository.findByEmail("a@example.com")).thenReturn(Optional.of(member));
        when(loginAttemptService.isLocked("a@example.com")).thenReturn(false);

        UserDetails result = memberUserDetailsService.loadUserByUsername("a@example.com");

        assertThat(result.getUsername()).isEqualTo("a@example.com");
    }

    @Test
    void loadUserByUsername_존재하지_않으면_예외를_던진다() {
        when(memberRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberUserDetailsService.loadUserByUsername("nobody@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_잠긴_계정이면_LockedException을_던진다() {
        Member member = Member.builder()
                .email("locked@example.com").nickname("테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").build();
        when(memberRepository.findByEmail("locked@example.com")).thenReturn(Optional.of(member));
        when(loginAttemptService.isLocked("locked@example.com")).thenReturn(true);

        assertThatThrownBy(() -> memberUserDetailsService.loadUserByUsername("locked@example.com"))
                .isInstanceOf(LockedException.class);
    }
}
