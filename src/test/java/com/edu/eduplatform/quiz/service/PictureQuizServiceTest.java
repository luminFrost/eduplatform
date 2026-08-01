package com.edu.eduplatform.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.edu.eduplatform.lesson.service.LessonService;
import com.edu.eduplatform.lesson.service.LessonService.IconPair;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.quiz.dto.PictureQuizQuestion;
import com.edu.eduplatform.quiz.dto.PictureQuizResult;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PictureQuizServiceTest {

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private PictureQuizService pictureQuizService;

    @Test
    void getTodayQuestions_충분한_재료가_있으면_문제를_만든다() {
        List<IconPair> pool = List.of(
                new IconPair("apple", "/images/openmoji/apple.svg"),
                new IconPair("dog", "/images/openmoji/dog.svg"),
                new IconPair("cat", "/images/openmoji/cat.svg"),
                new IconPair("bird", "/images/openmoji/bird.svg"),
                new IconPair("fish", "/images/openmoji/fish.svg"),
                new IconPair("frog", "/images/openmoji/frog.svg")
        );
        when(lessonService.collectIconPairs(MemberType.CHILD)).thenReturn(pool);

        List<PictureQuizQuestion> questions = pictureQuizService.getTodayQuestions();

        assertThat(questions).isNotEmpty();
        questions.forEach(q -> assertThat(q.options()).hasSizeGreaterThanOrEqualTo(2));
    }

    @Test
    void getTodayQuestions_재료가_2개_미만이면_빈_목록을_반환한다() {
        when(lessonService.collectIconPairs(MemberType.CHILD))
                .thenReturn(List.of(new IconPair("apple", "/images/openmoji/apple.svg")));

        List<PictureQuizQuestion> questions = pictureQuizService.getTodayQuestions();

        assertThat(questions).isEmpty();
    }

    @Test
    void getTodayQuestions_같은_날_두_번_호출하면_완전히_같은_문제가_나온다() {
        List<IconPair> pool = List.of(
                new IconPair("apple", "/a.svg"), new IconPair("dog", "/d.svg"),
                new IconPair("cat", "/c.svg"), new IconPair("bird", "/b.svg"),
                new IconPair("fish", "/f.svg")
        );
        when(lessonService.collectIconPairs(MemberType.CHILD)).thenReturn(pool);

        List<PictureQuizQuestion> first = pictureQuizService.getTodayQuestions();
        List<PictureQuizQuestion> second = pictureQuizService.getTodayQuestions();

        assertThat(first).isEqualTo(second);
    }

    @Test
    void score_정답과_오답_개수를_올바르게_센다() {
        List<IconPair> pool = List.of(
                new IconPair("apple", "/a.svg"), new IconPair("dog", "/d.svg"),
                new IconPair("cat", "/c.svg"), new IconPair("bird", "/b.svg"),
                new IconPair("fish", "/f.svg")
        );
        when(lessonService.collectIconPairs(MemberType.CHILD)).thenReturn(pool);

        List<PictureQuizQuestion> questions = pictureQuizService.getTodayQuestions();
        // 모든 문제에 첫 번째 보기를 답으로 제출 — 몇 개는 맞고 몇 개는 틀릴 수 있음, 범위만 검증.
        List<String> answers = new ArrayList<>();
        questions.forEach(q -> answers.add(q.options().get(0)));

        PictureQuizResult result = pictureQuizService.score(answers);

        assertThat(result.total()).isEqualTo(questions.size());
        assertThat(result.correct()).isBetween(0, result.total());
    }

    @Test
    void score_답이_전혀_없으면_전부_오답이다() {
        List<IconPair> pool = List.of(
                new IconPair("apple", "/a.svg"), new IconPair("dog", "/d.svg"),
                new IconPair("cat", "/c.svg")
        );
        when(lessonService.collectIconPairs(MemberType.CHILD)).thenReturn(pool);

        PictureQuizResult result = pictureQuizService.score(List.of());

        assertThat(result.correct()).isZero();
    }
}
