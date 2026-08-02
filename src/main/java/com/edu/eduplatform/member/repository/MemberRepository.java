package com.edu.eduplatform.member.repository;

import com.edu.eduplatform.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    List<Member> findByEmailContainingIgnoreCaseOrNicknameContainingIgnoreCase(String email, String nickname);
}
