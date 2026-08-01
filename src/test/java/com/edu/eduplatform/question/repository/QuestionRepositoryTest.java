package com.edu.eduplatform.question.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.domain.Question;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void save_보기_목록이_저장한_순서_그대로_조회된다() {
        Question question = questionRepository.save(Question.builder()
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).lessonType(LessonType.VOCAB)
                .prompt("빈칸에 알맞은 단어를 고르세요: I ___ to school.")
                .options(List.of("go", "goes", "going", "gone"))
                .correctOptionIndex(0)
                .build());

        Question found = questionRepository.findById(question.getId()).orElseThrow();

        // @OrderColumn 없이 실수하면 이 순서가 보장되지 않는다 — correctOptionIndex가 가리키는 보기가 틀어질 수 있다.
        assertThat(found.getOptions()).containsExactly("go", "goes", "going", "gone");
    }

    @Test
    void findByTargetTypeAndLevel_대상과_레벨이_일치하는_문항만_반환한다() {
        questionRepository.save(Question.builder()
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).lessonType(LessonType.VOCAB)
                .prompt("성인 초급 문제")
                .options(List.of("A", "B", "C", "D"))
                .correctOptionIndex(0)
                .build());
        questionRepository.save(Question.builder()
                .targetType(MemberType.CHILD).level(EnglishLevel.BEGINNER).lessonType(LessonType.VOCAB)
                .prompt("어린이 초급 문제")
                .options(List.of("A", "B", "C", "D"))
                .correctOptionIndex(0)
                .build());
        questionRepository.save(Question.builder()
                .targetType(MemberType.ADULT).level(EnglishLevel.ADVANCED).lessonType(LessonType.VOCAB)
                .prompt("성인 고급 문제")
                .options(List.of("A", "B", "C", "D"))
                .correctOptionIndex(0)
                .build());

        List<Question> result = questionRepository.findByTargetTypeAndLevel(MemberType.ADULT, EnglishLevel.BEGINNER);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrompt()).isEqualTo("성인 초급 문제");
    }
}
