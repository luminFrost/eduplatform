package com.edu.eduplatform.lesson.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class QuizWordPickerTest {

    @Test
    void extractKeyWord_불용어를_제외한_가장_긴_단어를_고른다() {
        Optional<String> answer = QuizWordPicker.extractKeyWord("I would like a coffee, please.");

        assertThat(answer).contains("coffee");
    }

    @Test
    void extractKeyWord_동률이면_문장에서_먼저_나오는_단어를_고른다() {
        // "showed"(6)와 "hidden"(6), "island"(6) 모두 6글자 — 먼저 나오는 "showed"가 이겨야 한다.
        Optional<String> answer = QuizWordPicker.extractKeyWord("The map showed a hidden island.");

        assertThat(answer).contains("showed");
    }

    @Test
    void extractKeyWord_불용어뿐이면_빈_값을_반환한다() {
        Optional<String> answer = QuizWordPicker.extractKeyWord("I am not this or that.");

        assertThat(answer).isEmpty();
    }

    @Test
    void blankOut_단어_첫_등장을_대소문자_무시하고_빈칸으로_바꾼다() {
        String result = QuizWordPicker.blankOut("I would like a Coffee, please.", "coffee");

        assertThat(result).isEqualTo("I would like a ___, please.");
    }

    @Test
    void pickDistractors_정답을_제외하고_최대_count개를_뽑는다() {
        List<String> pool = List.of("vacation", "beautiful", "coffee", "sunshine");

        List<String> distractors = QuizWordPicker.pickDistractors(pool, "coffee", 2, new Random(1));

        assertThat(distractors).hasSize(2).doesNotContain("coffee");
    }

    @Test
    void pickDistractors_후보가_count보다_적으면_있는_만큼만_반환한다() {
        List<String> pool = List.of("coffee", "vacation");

        List<String> distractors = QuizWordPicker.pickDistractors(pool, "coffee", 3, new Random(1));

        assertThat(distractors).containsExactly("vacation");
    }
}
