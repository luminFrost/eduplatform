package com.edu.eduplatform.quiz.service;

import com.edu.eduplatform.common.util.DailySeed;
import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse.ContentLine;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse.LineType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.lesson.service.LessonService;
import com.edu.eduplatform.lesson.service.QuizWordPicker;
import com.edu.eduplatform.member.dto.MemberResponse;
import com.edu.eduplatform.quiz.dto.DailyWord;
import com.edu.eduplatform.quiz.dto.DailyWordQuiz;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원의 대상·레벨에 맞는 공식 코스의 "English — 한글" 문장을 재료로, 오늘 날짜(+대상·레벨 조합)를 시드로
 * 매일 다른 단어장·단어 퀴즈를 즉석에서 만든다(stateless). 같은 날엔 같은 레벨의 모든 회원이 같은 문제를 본다
 * — 회원마다 다르게 시딩할 이유가 없어 더 단순한 쪽을 선택.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyWordService {

    private static final int WORD_COUNT = 5;
    private static final int OPTION_COUNT = 4;
    private static final long QUIZ_SEED_OFFSET = 1000;

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonService lessonService;

    public List<DailyWord> getTodayWords(MemberResponse member) {
        List<PhrasePair> pool = collectPhrasePool(member);
        if (pool.isEmpty()) {
            return List.of();
        }
        return DailySeed.shuffledForToday(pool, comboSeed(member)).stream()
                .limit(WORD_COUNT)
                .map(pair -> new DailyWord(pair.english(), pair.korean()))
                .toList();
    }

    public Optional<DailyWordQuiz> getTodayQuiz(MemberResponse member) {
        return pickTodayQuiz(member)
                .map(pick -> new DailyWordQuiz(pick.sentenceWithBlank(), pick.translation(), pick.options()));
    }

    public boolean checkAnswer(MemberResponse member, String submitted) {
        return pickTodayQuiz(member)
                .map(pick -> pick.answer().equalsIgnoreCase(submitted))
                .orElse(false);
    }

    private Optional<QuizPick> pickTodayQuiz(MemberResponse member) {
        List<PhrasePair> pool = collectPhrasePool(member);
        if (pool.size() < 2) {
            return Optional.empty();
        }
        long seed = comboSeed(member);
        PhrasePair chosen = DailySeed.shuffledForToday(pool, seed + QUIZ_SEED_OFFSET).get(0);
        Optional<String> keyWord = QuizWordPicker.extractKeyWord(chosen.english());
        if (keyWord.isEmpty()) {
            return Optional.empty();
        }
        String answer = keyWord.get();

        List<String> otherWords = pool.stream()
                .filter(pair -> pair != chosen)
                .map(pair -> QuizWordPicker.extractKeyWord(pair.english()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<String> distractors = QuizWordPicker.pickDistractors(
                otherWords, answer, OPTION_COUNT - 1, new Random(DailySeed.forToday() + seed));
        if (distractors.isEmpty()) {
            return Optional.empty();
        }

        List<String> options = new ArrayList<>(distractors);
        options.add(answer);
        Collections.shuffle(options, new Random(DailySeed.forToday() + seed));

        return Optional.of(new QuizPick(
                QuizWordPicker.blankOut(chosen.english(), answer), chosen.korean(), options, answer));
    }

    private List<PhrasePair> collectPhrasePool(MemberResponse member) {
        Map<String, PhrasePair> pairsByEnglish = new LinkedHashMap<>();
        for (Course course : courseRepository.search(member.memberType(), member.level(), null)) {
            for (Lesson lesson : lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId())) {
                for (ContentLine line : lessonService.parseContent(lesson.getContent())) {
                    if (line.type() != LineType.PHRASE) {
                        continue;
                    }
                    pairsByEnglish.putIfAbsent(line.text().toLowerCase(), new PhrasePair(line.text(), line.subtext()));
                }
            }
        }
        return List.copyOf(pairsByEnglish.values());
    }

    private long comboSeed(MemberResponse member) {
        return member.memberType().ordinal() * 31L + member.level().ordinal();
    }

    private record PhrasePair(String english, String korean) {
    }

    private record QuizPick(String sentenceWithBlank, String translation, List<String> options, String answer) {
    }
}
