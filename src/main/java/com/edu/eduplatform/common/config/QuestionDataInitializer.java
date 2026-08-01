package com.edu.eduplatform.common.config;

import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.domain.Question;
import com.edu.eduplatform.question.repository.QuestionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 진단 테스트(DIAGNOSTIC_TEST) 문항 시드.
 * 대상(CHILD/ADULT) x 레벨(4단계) x 영역(어휘/읽기/쓰기/듣기/말하기) 조합마다 2문항씩, 총 80문항.
 * Course/Lesson과 무관하게 독립적으로 채점 가능해야 해서(듣기·말하기도 문제를 낼 수 있어야 함) Question은
 * lessonId가 아니라 lessonType을 직접 갖는다 — Question.java 클래스 주석 참고.
 * Course/Lesson 시드(SampleDataInitializer)와 무관하게 독립적으로 시드 가능해 별도 클래스로 뒀다
 * (SampleDataInitializer가 이미 1400줄 넘어 더 안 키우려는 목적).
 */
@Component
@RequiredArgsConstructor
public class QuestionDataInitializer implements CommandLineRunner {

    private final QuestionRepository questionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (questionRepository.count() > 0) {
            return;
        }
        // buildQuestions()를 매번 새로 호출해 새 엔티티(id == null)를 만든다 — static 필드에 이미 만든 엔티티를
        // 캐싱해두면, JPA IDENTITY 채번으로 첫 저장 때 id가 그 객체에 그대로 박혀버려서(정적 필드라 JVM 내내 유지됨)
        // 테스트 스위트에서 스프링 컨텍스트가 다시 뜰 때(빈 DB인데 이미 id가 있는 상태로) saveAll()이
        // persist 대신 merge를 타면서 StaleObjectStateException이 난다 — 실제로 겪은 버그.
        questionRepository.saveAll(buildQuestions());
    }

    private static List<Question> buildQuestions() {
        return List.of(

            // ---------- CHILD / BEGINNER ----------
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: I have a blue ___.")
                    .options(List.of("hat", "dog", "cake", "tree"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.VOCAB)
                    .prompt("다음 중 '강아지'를 뜻하는 단어를 고르세요.")
                    .options(List.of("dog", "cat", "rabbit", "bear"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'I am Tom. I am seven.' Tom은 몇 살인가요?")
                    .options(List.of("일곱 살", "여덟 살", "여섯 살", "아홉 살"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'Thank you! You're welcome.' 이 대화에서 알 수 있는 것은 무엇인가요?")
                    .options(List.of("고마움을 표현하고 있다", "화가 나 있다", "작별 인사를 하고 있다", "질문을 하고 있다"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of("I am happy.", "I is happy.", "I are happy.", "Happy I am."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of("This is my dog.", "This are my dog.", "This is my dogs.", "This my dog is."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("Stand up.")
                    .options(List.of("Stand up.", "Sit down.", "Line up.", "Wash your hands."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("Open your book.")
                    .options(List.of("Open your book.", "Close your book.", "Raise your hand.", "Look at the board."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 누군가 'Hi!'라고 인사했습니다.")
                    .options(List.of("Hi!", "I'm sorry.", "Good job!", "Wait, please."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 누군가 처음 만나서 'Nice to meet you.'라고 말했습니다.")
                    .options(List.of("Nice to meet you, too.", "See you later.", "Excuse me.", "No, thank you."))
                    .correctOptionIndex(0)
                    .build(),

            // ---------- CHILD / ELEMENTARY ----------
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: A dog says ___.")
                    .options(List.of("woof", "meow", "moo", "tweet"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: I need a ___ to write.")
                    .options(List.of("pencil", "backpack", "lunch", "umbrella"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'Sam wakes up early. Sam brushes his teeth. Sam gets dressed.' Sam이 가장 먼저 하는 일은 무엇인가요?")
                    .options(List.of("일어나기", "이 닦기", "옷 입기", "아침 먹기"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'Sam has a friend. Her name is Amy. They play together.' Amy는 누구인가요?")
                    .options(List.of("Sam의 친구", "Sam의 여동생", "Sam의 선생님", "Sam의 이웃"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of("He plays soccer every day.", "He play soccer every day.", "He playing soccer every day.", "He plays soccer every days."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of("We have lunch at noon.", "We has lunch at noon.", "We having lunch at noon.", "We have lunch on noon."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("Class starts now.")
                    .options(List.of("Class starts now.", "Lunch is ready.", "It's time for recess.", "School is over."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("Please wash your hands first.")
                    .options(List.of("Please wash your hands first.", "Please clean your desk.", "Please bring your umbrella.", "Please pack your bag."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 누군가 'What's your name?'이라고 물었습니다.")
                    .options(List.of("My name is Mia.", "I am eight years old.", "I like dogs.", "I live in Seoul."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 누군가 'How old are you?'라고 물었습니다.")
                    .options(List.of("I am eight years old.", "My name is Ben.", "I live near the school.", "I like blue."))
                    .correctOptionIndex(0)
                    .build(),

            // ---------- CHILD / INTERMEDIATE ----------
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: My favorite ___ is science.")
                    .options(List.of("subject", "season", "hobby", "food"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: It's raining, so bring an ___.")
                    .options(List.of("umbrella", "backpack", "pencil", "cookie"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'A: What are you doing this weekend? B: I'm going to ride my bike with my brother.' B는 주말에 무엇을 할 건가요?")
                    .options(List.of("자전거를 탈 것이다", "숙제를 할 것이다", "동물원에 갈 것이다", "책을 읽을 것이다"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'A: Excuse me, how much is this shirt? B: It's ten dollars.' 셔츠의 가격은 얼마인가요?")
                    .options(List.of("10달러", "5달러", "20달러", "1달러"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of("I played soccer yesterday.", "I play soccer yesterday.", "I playing soccer yesterday.", "I plays soccer yesterday."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of("We will visit my grandma tomorrow.", "We visit will my grandma tomorrow.", "We will visited my grandma tomorrow.", "We will visits my grandma tomorrow."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("Let's go to the park.")
                    .options(List.of("Let's go to the park.", "Let's go to the zoo.", "Let's go to school.", "Let's go home."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("Have you seen my umbrella?")
                    .options(List.of("Have you seen my umbrella?", "Have you seen my book?", "Have you seen my dog?", "Have you seen my bag?"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 누군가 'What do you think?'라고 물었습니다.")
                    .options(List.of("I think it's a good idea.", "I am eight years old.", "I have a dog.", "See you later."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 누군가 'Do you agree with me?'라고 물었습니다.")
                    .options(List.of("Yes, I agree with you.", "I like pizza.", "It's raining today.", "My name is Tom."))
                    .correctOptionIndex(0)
                    .build(),

            // ---------- CHILD / ADVANCED ----------
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: Once upon a time, there ___ a kind queen.")
                    .options(List.of("was", "is", "were", "be"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: The brave knight ___ the dragon at the castle gate.")
                    .options(List.of("fought", "fighted", "fights", "fighting"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'A hungry fox saw juicy grapes on a tall vine. He jumped again and again, but he could not reach them.' 여우는 왜 포도를 먹지 못했나요?")
                    .options(List.of("너무 높이 있어서", "포도가 썩어서", "배가 불러서", "포도가 없어서"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'A small mouse woke a sleeping lion by accident. The lion let the mouse go instead of eating it.' 사자는 생쥐에게 어떻게 했나요?")
                    .options(List.of("놓아주었다", "잡아먹었다", "화를 냈다", "도망쳤다"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 이야기의 시작 문장으로 가장 자연스러운 것을 고르세요.")
                    .options(List.of("Once upon a time, there was a little girl.", "Once a time upon, there was a little girl.", "There was upon a time a little girl.", "A little girl there was, once upon time."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of("The princess was brave and very kind.", "The princess were brave and very kind.", "The princess brave and very kind was.", "The princess is being brave and very kind was."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("A tiny fairy lived inside a flower.")
                    .options(List.of("A tiny fairy lived inside a flower.", "A tiny frog lived inside a pond.", "A tiny bird lived inside a nest.", "A tiny bear lived inside a cave."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("The knight and the dragon became friends.")
                    .options(List.of("The knight and the dragon became friends.", "The knight and the wizard became enemies.", "The princess and the dragon became friends.", "The knight and the dragon ran away."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 이야기 속 친구가 'What happened next?'라고 물었습니다.")
                    .options(List.of("Then, they found a hidden treasure.", "Nice to meet you.", "I'm sorry about that.", "See you tomorrow."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.CHILD)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 선생님이 'What is your favorite story about?'라고 물었습니다.")
                    .options(List.of("My favorite story is about a brave lion.", "I have a red apple.", "It's three thirty.", "Go straight and turn left."))
                    .correctOptionIndex(0)
                    .build(),

            // ---------- ADULT / BEGINNER ----------
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: I ___ a coffee, please.")
                    .options(List.of("would like", "would liking", "liked to", "am like"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: Excuse me, how do I get ___ the station?")
                    .options(List.of("to", "at", "in", "on"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'I have a reservation. What time is check-out?' 화자는 지금 어디에 있나요?")
                    .options(List.of("호텔", "식당", "공항", "은행"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'Can you take me to this address? How much is the fare?' 화자는 무엇을 타고 있나요?")
                    .options(List.of("택시", "비행기", "지하철", "자전거"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of("How much is this?", "How much this is?", "How much are this?", "How much this be?"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of("I wake up at seven.", "I wakes up at seven.", "I waking up at seven.", "I woken up at seven."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("What time is it?")
                    .options(List.of("What time is it?", "What's your name?", "Where are you from?", "How old are you?"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("The store will close in ten minutes.")
                    .options(List.of("The store will close in ten minutes.", "The store will open in ten minutes.", "The train will leave in ten minutes.", "The meeting will start in ten minutes."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 누군가 'How was your weekend?'라고 물었습니다.")
                    .options(List.of("I just stayed home and rested.", "I have a red apple.", "It's three thirty.", "Go straight and turn left."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.BEGINNER)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 식당 종업원이 'Can I get you anything else?'라고 물었습니다.")
                    .options(List.of("No, thanks. Just the bill, please.", "Nice to meet you.", "See you later.", "I'm sorry about that."))
                    .correctOptionIndex(0)
                    .build(),

            // ---------- ADULT / ELEMENTARY ----------
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: Let's ___ started with today's meeting.")
                    .options(List.of("get", "gets", "getting", "got"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: I'm writing to ___ our meeting scheduled for 10 a.m.")
                    .options(List.of("confirm", "confirming", "confirmed", "confirms"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'I'm following up on my previous email. Just checking if you had a chance to review this.' 이 이메일의 목적은 무엇인가요?")
                    .options(List.of("이전 이메일에 대한 답변 확인", "새로운 회의 요청", "사과 전달", "감사 인사"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'The meeting room is booked until two. You can book another room online.' 지금 이 회의실을 사용하지 못하는 이유는 무엇인가요?")
                    .options(List.of("이미 예약되어 있어서", "고장 나서", "청소 중이어서", "공사 중이어서"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 이메일 도입부로 가장 자연스러운 문장을 고르세요.")
                    .options(List.of("I hope this email finds you well.", "I hope this email find you well.", "I hope this email finding you well.", "I hope this email found you well."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of("Could you send me that file today?", "Could you sends me that file today?", "Could you sending me that file today?", "Could you sent me that file today?"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("The office will open at nine today.")
                    .options(List.of("The office will open at nine today.", "The office will close at nine today.", "The office opened at nine yesterday.", "The store will open at nine today."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("Please leave your name and number.")
                    .options(List.of("Please leave your name and number.", "Please leave your bag and coat.", "Please leave your name and address.", "Please leave a message after the tone."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 동료가 'Do you want to grab lunch together?'라고 말했습니다.")
                    .options(List.of("Sure, where should we go?", "Nice to meet you.", "I lost my passport.", "How much is this?"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ELEMENTARY)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 상사가 'Can you send me that report today?'라고 말했습니다.")
                    .options(List.of("Sure, I'll send it this afternoon.", "See you tomorrow.", "I'm not sure about that.", "It was nice talking to you."))
                    .correctOptionIndex(0)
                    .build(),

            // ---------- ADULT / INTERMEDIATE ----------
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: We need to ___ the deadline by at least one week.")
                    .options(List.of("extend", "extending", "extends", "extended"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: Could you send me the ___ report before Friday's meeting?")
                    .options(List.of("quarterly", "quarter", "quartering", "quarterize"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'We are on track to complete the first phase by the end of this month. A more detailed report will follow once testing is finished.' 이 이메일의 목적은 무엇인가요?")
                    .options(List.of("프로젝트 진행 상황 보고", "예산 승인 요청", "회의 일정 취소 안내", "신규 고객 소개"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'I'm afraid the current deadline may not be feasible given the scope of the work. Would it be possible to extend it by one week?' 글쓴이가 요청하는 것은 무엇인가요?")
                    .options(List.of("마감일 연장", "예산 증액", "팀원 추가 배정", "회의 취소"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of(
                            "Would it be possible to schedule a meeting sometime next week?",
                            "Would it be possible schedule a meeting sometime next week?",
                            "Would it be possible to scheduling a meeting sometime next week?",
                            "Would it possible to be schedule a meeting sometime next week?"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of(
                            "I apologize for the delay and assure you this won't happen again.",
                            "I apologize for the delay and assure you this don't happen again.",
                            "I apologize about the delay and assure you this won't happening again.",
                            "I apologize for delay and assure you this won't happen again."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("Today's meeting will cover three main topics.")
                    .options(List.of(
                            "Today's meeting will cover three main topics.",
                            "Today's meeting will start at three o'clock.",
                            "Today's report covers three main topics.",
                            "Yesterday's meeting covered three main topics."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("As you can see on this slide, revenue grew by fifteen percent.")
                    .options(List.of(
                            "As you can see on this slide, revenue grew by fifteen percent.",
                            "As you can see on this slide, revenue fell by fifteen percent.",
                            "As you can see on this chart, revenue grew by fifty percent.",
                            "As you can see on this slide, expenses grew by fifteen percent."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 동료가 'I have a few concerns about the new timeline.'이라고 말했습니다.")
                    .options(List.of(
                            "I understand your concerns — could we discuss ways to manage that risk?",
                            "See you at the meeting tomorrow.",
                            "I'm sorry, I don't work here anymore.",
                            "That has nothing to do with me."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.INTERMEDIATE)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 상대방이 'Would it be possible to lower the price slightly?'라고 협상해 왔습니다.")
                    .options(List.of(
                            "In exchange, we could commit to a longer contract.",
                            "Nice to meet you, welcome aboard.",
                            "I'll be out of the office next week.",
                            "That's not a question I can answer."))
                    .correctOptionIndex(0)
                    .build(),

            // ---------- ADULT / ADVANCED ----------
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: I ___ that there are valid concerns, but my position remains unchanged.")
                    .options(List.of("concede", "conceding", "concedes", "conceded to"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.VOCAB)
                    .prompt("빈칸에 알맞은 단어를 고르세요: That ___ doesn't necessarily hold true once you examine the underlying data.")
                    .options(List.of("assumption", "assuming", "assumes", "assumed"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'The first viewpoint emphasizes individual responsibility, while the second focuses on social structures. Comparing both views helps us understand the complexity of the issue.' 글쓴이가 이 문단에서 하고자 하는 것은 무엇인가요?")
                    .options(List.of("두 관점을 비교하여 사안의 복잡성을 보여주기", "한 관점의 오류를 지적하기", "새로운 정책을 제안하기", "통계 자료를 인용하기"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.READING)
                    .prompt("다음을 읽고 답하세요: 'The author's conclusion seems well-supported by the evidence presented. However, the conclusion overlooks an important counterargument.' 이 문단에서 글쓴이의 태도는 어떠한가요?")
                    .options(List.of("결론을 부분적으로 인정하면서도 한계를 지적함", "결론에 전적으로 동의함", "결론을 완전히 거부함", "결론에 대해 언급하지 않음"))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of(
                            "Critics argue that the policy fails to address the root cause, yet supporters point to early signs of progress.",
                            "Critics argue that the policy fail to address the root cause, yet supporters point to early signs of progress.",
                            "Critics argue that the policy fails to addressing the root cause, yet supporters points to early signs of progress.",
                            "Critics arguing that the policy fails to address the root cause, yet supporters point to early sign of progress."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.WRITING)
                    .prompt("다음 중 문법적으로 올바른 문장을 고르세요.")
                    .options(List.of(
                            "Had the committee reviewed the proposal more carefully, the funding might not have been approved.",
                            "Had the committee review the proposal more carefully, the funding might not have been approved.",
                            "If the committee had reviewed the proposal more carefully, the funding might not been approved.",
                            "Had the committee reviewed the proposal more carefully, the funding might not being approved."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("Critics argue that the policy fails to address the root cause.")
                    .options(List.of(
                            "Critics argue that the policy fails to address the root cause.",
                            "Critics argue that the policy fails to address the budget deficit.",
                            "Supporters argue that the policy fails to address the root cause.",
                            "Critics argue that the policy succeeds in addressing the root cause."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.LISTENING)
                    .prompt("다음을 듣고, 들은 문장을 고르세요.")
                    .audioText("Nearly sixty percent of respondents support the proposal.")
                    .options(List.of(
                            "Nearly sixty percent of respondents support the proposal.",
                            "Nearly sixty percent of respondents oppose the proposal.",
                            "Nearly sixteen percent of respondents support the proposal.",
                            "Nearly sixty percent of lawmakers support the proposal."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 면접관이 'Tell me about a time you had to make a difficult decision under a tight deadline.'라고 물었습니다.")
                    .options(List.of(
                            "Let me give you a specific example from my last role, where I had to prioritize under significant time pressure.",
                            "I'd rather not talk about my past experience.",
                            "That sounds like a difficult question for you to answer.",
                            "I'm not sure what deadlines are."))
                    .correctOptionIndex(0)
                    .build(),
            Question.builder()
                    .targetType(MemberType.ADULT)
                    .level(EnglishLevel.ADVANCED)
                    .lessonType(LessonType.SPEAKING)
                    .prompt("다음 상황에서 가장 자연스러운 대답을 고르세요: 토론 상대가 'The evidence simply doesn't support that conclusion.'이라고 반박했습니다.")
                    .options(List.of(
                            "That's a fair point, but the broader data still suggests the trend is real.",
                            "I don't have anything to say about that.",
                            "Let's talk about something else entirely.",
                            "You are clearly wrong about everything."))
                    .correctOptionIndex(0)
                    .build()
        );
    }
}
