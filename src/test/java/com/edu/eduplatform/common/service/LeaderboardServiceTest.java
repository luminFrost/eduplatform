package com.edu.eduplatform.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.common.dto.LeaderboardEntry;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.progress.dto.MemberActivityStats;
import com.edu.eduplatform.progress.service.ProgressService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private ProgressService progressService;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Test
    void getTopEntries_스트릭_내림차순_완료수_내림차순_memberId_오름차순으로_정렬한다() throws Exception {
        when(progressService.getLeaderboardStats()).thenReturn(List.of(
                new MemberActivityStats(1L, 5, 3),
                new MemberActivityStats(2L, 10, 5),
                new MemberActivityStats(3L, 10, 5)));
        when(memberRepository.findAllById(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                member(1L, "member1"), member(2L, "member2"), member(3L, "member3")));

        List<LeaderboardEntry> entries = leaderboardService.getTopEntries(null);

        assertThat(entries).extracting(LeaderboardEntry::rank).containsExactly(1, 2, 3);
        assertThat(entries).extracting(e -> e.nickname())
                .containsExactly("member2", "member3", "member1");
        assertThat(entries).allSatisfy(e -> assertThat(e.currentMember()).isFalse());
    }

    @Test
    void getTopEntries_상위_20명으로_자른다() throws Exception {
        List<MemberActivityStats> stats = new ArrayList<>();
        List<Member> members = new ArrayList<>();
        for (long i = 1; i <= 25; i++) {
            stats.add(new MemberActivityStats(i, (int) i, (int) i));
            members.add(member(i, "member" + i));
        }
        when(progressService.getLeaderboardStats()).thenReturn(stats);
        when(memberRepository.findAllById(org.mockito.ArgumentMatchers.any())).thenReturn(members);

        List<LeaderboardEntry> entries = leaderboardService.getTopEntries(null);

        assertThat(entries).hasSize(20);
        assertThat(entries.get(0).nickname()).isEqualTo("member25");
    }

    @Test
    void getTopEntries_본인이_상위권_밖이면_목록_끝에_본인_행을_추가한다() throws Exception {
        List<MemberActivityStats> stats = new ArrayList<>();
        List<Member> members = new ArrayList<>();
        for (long i = 1; i <= 25; i++) {
            stats.add(new MemberActivityStats(i, (int) i, (int) i));
            members.add(member(i, "member" + i));
        }
        when(progressService.getLeaderboardStats()).thenReturn(stats);
        when(memberRepository.findAllById(org.mockito.ArgumentMatchers.any())).thenReturn(members);

        // 순위가 가장 낮은(스트릭 1) memberId=1L은 25위 → 상위 20위 밖.
        List<LeaderboardEntry> entries = leaderboardService.getTopEntries(1L);

        assertThat(entries).hasSize(21);
        LeaderboardEntry self = entries.get(20);
        assertThat(self.currentMember()).isTrue();
        assertThat(self.rank()).isEqualTo(25);
        assertThat(self.nickname()).isEqualTo("member1");
    }

    @Test
    void getTopEntries_본인이_상위권_안이면_중복으로_추가하지_않는다() throws Exception {
        when(progressService.getLeaderboardStats()).thenReturn(List.of(
                new MemberActivityStats(1L, 5, 3),
                new MemberActivityStats(2L, 10, 5)));
        when(memberRepository.findAllById(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                member(1L, "member1"), member(2L, "member2")));

        List<LeaderboardEntry> entries = leaderboardService.getTopEntries(2L);

        assertThat(entries).hasSize(2);
        assertThat(entries).filteredOn(LeaderboardEntry::currentMember).hasSize(1);
    }

    @Test
    void getTopEntries_순위가_없는_회원이면_본인_행을_추가하지_않는다() throws Exception {
        when(progressService.getLeaderboardStats()).thenReturn(List.of(
                new MemberActivityStats(1L, 5, 3)));
        when(memberRepository.findAllById(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                member(1L, "member1")));

        // memberId=99L은 완료 기록이 없어 순위 자체가 없다.
        List<LeaderboardEntry> entries = leaderboardService.getTopEntries(99L);

        assertThat(entries).hasSize(1);
        assertThat(entries).allSatisfy(e -> assertThat(e.currentMember()).isFalse());
    }

    private static Member member(Long id, String nickname) throws Exception {
        Member member = Member.builder()
                .email(nickname + "@example.com")
                .nickname(nickname)
                .password("password")
                .memberType(MemberType.ADULT)
                .level(EnglishLevel.BEGINNER)
                .build();
        Field field = Member.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(member, id);
        return member;
    }
}
