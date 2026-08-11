package com.edu.eduplatform.common.service;

import com.edu.eduplatform.common.dto.LeaderboardEntry;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.progress.dto.MemberActivityStats;
import com.edu.eduplatform.progress.service.ProgressService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 스트릭·완료 레슨 수로 전체 회원 순위를 매긴다. Progress와 Member 두 도메인을 넘나드는 집계라
 * {@link ContentCoverageService}/{@link AdminStatsService}와 같은 이유로 특정 도메인 서비스에
 * 얹지 않고 여기 둔다. 새 추적 테이블 없이 기존 {@link ProgressService#getLeaderboardStats()}만
 * 으로 매 요청마다 즉석 계산한다(stateless).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

    private static final int TOP_N = 20;

    private final ProgressService progressService;
    private final MemberRepository memberRepository;

    /**
     * 스트릭 내림차순 → 완료 레슨 수 내림차순 → memberId 오름차순으로 정렬한 상위 {@link #TOP_N}명을
     * 반환한다. currentMemberId가 상위권 밖이면(순위가 있는 한) 목록 끝에 본인 행을 하나 더 붙인다.
     */
    public List<LeaderboardEntry> getTopEntries(Long currentMemberId) {
        List<MemberActivityStats> ranked = rankedStats();
        Map<Long, String> nicknames = nicknamesFor(ranked);

        List<LeaderboardEntry> top = new ArrayList<>(toEntries(
                ranked.subList(0, Math.min(TOP_N, ranked.size())), nicknames, currentMemberId, 1));

        boolean alreadyIncluded = top.stream().anyMatch(LeaderboardEntry::currentMember);
        if (currentMemberId != null && !alreadyIncluded) {
            int index = indexOfMember(ranked, currentMemberId);
            if (index >= 0) {
                top.add(toEntry(ranked.get(index), nicknames, currentMemberId, index + 1));
            }
        }
        return top;
    }

    private List<MemberActivityStats> rankedStats() {
        return progressService.getLeaderboardStats().stream()
                .sorted(Comparator.comparingInt(MemberActivityStats::currentStreak).reversed()
                        .thenComparing(Comparator.comparingInt(MemberActivityStats::completedLessons).reversed())
                        .thenComparing(MemberActivityStats::memberId))
                .toList();
    }

    private Map<Long, String> nicknamesFor(List<MemberActivityStats> ranked) {
        List<Long> memberIds = ranked.stream().map(MemberActivityStats::memberId).toList();
        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));
    }

    private static int indexOfMember(List<MemberActivityStats> ranked, Long memberId) {
        for (int i = 0; i < ranked.size(); i++) {
            if (ranked.get(i).memberId().equals(memberId)) {
                return i;
            }
        }
        return -1;
    }

    private static List<LeaderboardEntry> toEntries(List<MemberActivityStats> stats,
            Map<Long, String> nicknames, Long currentMemberId, int startRank) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = startRank;
        for (MemberActivityStats stat : stats) {
            entries.add(toEntry(stat, nicknames, currentMemberId, rank));
            rank++;
        }
        return entries;
    }

    private static LeaderboardEntry toEntry(MemberActivityStats stat, Map<Long, String> nicknames,
            Long currentMemberId, int rank) {
        return new LeaderboardEntry(
                rank,
                nicknames.getOrDefault(stat.memberId(), "알 수 없는 회원"),
                stat.completedLessons(),
                stat.currentStreak(),
                stat.memberId().equals(currentMemberId));
    }
}
