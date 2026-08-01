package com.edu.eduplatform.quiz.service;

import com.edu.eduplatform.common.util.DailySeed;
import com.edu.eduplatform.lesson.service.LessonService;
import com.edu.eduplatform.lesson.service.LessonService.IconPair;
import com.edu.eduplatform.lesson.service.QuizWordPicker;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.quiz.dto.PictureQuizQuestion;
import com.edu.eduplatform.quiz.dto.PictureQuizResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 아이가 그림을 보고 단어를 맞히는 퀴즈. CHILD 공식 코스에 이미 있는 레슨 카드 아이콘을 재료로 쓴다 —
 * 새 콘텐츠 없이 오늘 날짜를 시드로 매일 다른 5문제를 즉석에서 만든다(stateless, DB 저장 없음).
 */
@Service
@RequiredArgsConstructor
public class PictureQuizService {

    private static final int QUESTION_COUNT = 5;
    private static final int OPTION_COUNT = 4;

    private final LessonService lessonService;

    public List<PictureQuizQuestion> getTodayQuestions() {
        return pickTodayQuestions().stream()
                .map(pick -> new PictureQuizQuestion(pick.iconImage(), pick.options()))
                .toList();
    }

    public PictureQuizResult score(List<String> submittedAnswers) {
        List<PictureQuizPick> today = pickTodayQuestions();
        int correct = 0;
        for (int i = 0; i < today.size(); i++) {
            String submitted = i < submittedAnswers.size() ? submittedAnswers.get(i) : null;
            if (today.get(i).correctWord().equalsIgnoreCase(submitted)) {
                correct++;
            }
        }
        return new PictureQuizResult(correct, today.size());
    }

    private List<PictureQuizPick> pickTodayQuestions() {
        List<IconPair> pool = lessonService.collectIconPairs(MemberType.CHILD);
        if (pool.size() < 2) {
            return List.of();
        }
        List<String> allWords = pool.stream().map(IconPair::word).toList();
        List<IconPair> picks = DailySeed.shuffledForToday(pool, 0)
                .subList(0, Math.min(QUESTION_COUNT, pool.size()));

        List<PictureQuizPick> result = new ArrayList<>();
        for (IconPair pick : picks) {
            long seed = DailySeed.forToday() + pick.word().toLowerCase().hashCode();
            List<String> distractors = QuizWordPicker.pickDistractors(
                    allWords, pick.word(), OPTION_COUNT - 1, new Random(seed));
            if (distractors.isEmpty()) {
                continue;
            }
            List<String> options = new ArrayList<>(distractors);
            options.add(pick.word());
            Collections.shuffle(options, new Random(seed));
            result.add(new PictureQuizPick(pick.iconImage(), pick.word(), options));
        }
        return result;
    }

    private record PictureQuizPick(String iconImage, String correctWord, List<String> options) {
    }
}
