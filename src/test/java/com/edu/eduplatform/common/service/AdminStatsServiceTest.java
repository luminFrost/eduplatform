package com.edu.eduplatform.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.common.entity.BaseTimeEntity;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.Member;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.member.repository.MemberRepository;
import com.edu.eduplatform.progress.domain.LearningProgress;
import com.edu.eduplatform.progress.repository.LearningProgressRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LearningProgressRepository learningProgressRepository;

    @InjectMocks
    private AdminStatsService adminStatsService;

    @Test
    void getSummary_전체_회원수와_최근_7일_신규가입자수를_계산한다() throws Exception {
        Member recent = withCreatedAt(newMember("recent@example.com"), LocalDateTime.now().minusDays(2));
        Member old = withCreatedAt(newMember("old@example.com"), LocalDateTime.now().minusDays(30));
        when(memberRepository.findAll()).thenReturn(List.of(recent, old));
        when(learningProgressRepository.findAll()).thenReturn(List.of());

        var summary = adminStatsService.getSummary();

        assertThat(summary.totalMembers()).isEqualTo(2);
        assertThat(summary.newMembersLast7Days()).isEqualTo(1);
    }

    @Test
    void getSummary_완료된_기록만_누적_완료_건수에_반영한다() throws Exception {
        LearningProgress completed = withCompletedAt(newCompleted(), LocalDateTime.now());
        LearningProgress notCompleted = LearningProgress.builder().memberId(1L).lessonId(2L).build();
        when(memberRepository.findAll()).thenReturn(List.of());
        when(learningProgressRepository.findAll()).thenReturn(List.of(completed, notCompleted));

        var summary = adminStatsService.getSummary();

        assertThat(summary.totalCompletions()).isEqualTo(1);
    }

    @Test
    void getSummary_14일치_추이_배열과_최댓값을_계산한다() throws Exception {
        Member m1 = withCreatedAt(newMember("a@example.com"), LocalDateTime.now());
        Member m2 = withCreatedAt(newMember("b@example.com"), LocalDateTime.now());
        when(memberRepository.findAll()).thenReturn(List.of(m1, m2));
        when(learningProgressRepository.findAll()).thenReturn(List.of());

        var summary = adminStatsService.getSummary();

        assertThat(summary.dailySignups()).hasSize(14);
        assertThat(summary.maxDailySignup()).isEqualTo(2);
        assertThat(summary.dailySignups().get(13).completedCount()).isEqualTo(2);
    }

    private static Member newMember(String email) {
        return Member.builder()
                .email(email).nickname("테스터")
                .memberType(MemberType.ADULT).level(EnglishLevel.BEGINNER)
                .password("hashed").build();
    }

    private static LearningProgress newCompleted() {
        LearningProgress progress = LearningProgress.builder().memberId(1L).lessonId(1L).build();
        progress.complete();
        return progress;
    }

    private static Member withCreatedAt(Member member, LocalDateTime createdAt) throws Exception {
        Field field = BaseTimeEntity.class.getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(member, createdAt);
        return member;
    }

    private static LearningProgress withCompletedAt(LearningProgress progress, LocalDateTime completedAt) throws Exception {
        Field field = LearningProgress.class.getDeclaredField("completedAt");
        field.setAccessible(true);
        field.set(progress, completedAt);
        return progress;
    }
}
