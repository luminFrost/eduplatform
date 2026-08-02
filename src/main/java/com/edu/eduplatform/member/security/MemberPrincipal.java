package com.edu.eduplatform.member.security;

import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberRole;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 인증된 회원을 나타내는 UserDetails 구현.
 * memberId를 직접 들고 있어 {@link com.edu.eduplatform.common.web.CurrentMemberIdArgumentResolver}가
 * 추가 DB 조회 없이 회원 id를 꺼내 쓸 수 있다.
 */
public class MemberPrincipal implements UserDetails {

    private final Long memberId;
    private final String email;
    private final String password;
    private final MemberRole role;

    private MemberPrincipal(Long memberId, String email, String password, MemberRole role) {
        this.memberId = memberId;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static MemberPrincipal of(Member member) {
        return new MemberPrincipal(member.getId(), member.getEmail(), member.getPassword(), member.getRole());
    }

    public Long getMemberId() {
        return memberId;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_MEMBER"));
        if (role == MemberRole.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }
}
