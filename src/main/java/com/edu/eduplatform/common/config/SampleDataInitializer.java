package com.edu.eduplatform.common.config;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개발용 샘플 코스·레슨 데이터.
 * 대상(CHILD/ADULT) x 레벨(4단계) 조합마다 여러 코스를 채워 목록/필터 화면을 실제 데이터로 확인할 수 있게 한다.
 * 초등(CHILD) 코스는 레슨마다 "INTRO:" 줄로 여우 선생님 마스코트 인사말을 넣어 시각적으로 심심하지 않게 한다
 * (실제 삽화 도입 전까지 이모지로 대체 — CLAUDE.md 작업 상태 참고).
 * 레슨마다 LessonType(어휘/읽기/쓰기)을 태그해 코스 상세 화면의 영역별 탭에서 필터링할 수 있게 한다.
 * 듣기·말하기는 MVP 이후 오디오/음성인식 도입 시 채워질 예정이라 아직 태그된 레슨이 없다.
 */
@Component
@RequiredArgsConstructor
public class SampleDataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (courseRepository.count() > 0) {
            return;
        }

        for (CourseSeed seed : COURSE_SEEDS) {
            Course course = courseRepository.save(Course.builder()
                    .title(seed.title())
                    .description(seed.description())
                    .emoji(seed.emoji())
                    .targetType(seed.targetType())
                    .level(seed.level())
                    .build());

            int orderNo = 1;
            for (LessonSeed lesson : seed.lessons()) {
                lessonRepository.save(Lesson.builder()
                        .courseId(course.getId())
                        .orderNo(orderNo++)
                        .title(lesson.title())
                        .content(lesson.content())
                        .lessonType(lesson.lessonType())
                        .build());
            }
        }
    }

    private record LessonSeed(String title, LessonType lessonType, String content) {
    }

    private record CourseSeed(
            String title,
            String description,
            String emoji,
            MemberType targetType,
            EnglishLevel level,
            List<LessonSeed> lessons
    ) {
    }

    private static final List<CourseSeed> COURSE_SEEDS = List.of(

            // ---------- CHILD / BEGINNER ----------
            new CourseSeed("파닉스로 시작하는 알파벳", "그림과 소리로 배우는 첫 알파벳, 짧은 레슨 위주로 구성했습니다.", "🔤",
                    MemberType.CHILD, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("알파벳 A부터 E까지", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 배우는 알파벳!
                            🍎 A는 Apple(사과)의 A예요.
                            🍌 B는 Banana(바나나)의 B예요.
                            🐱 C는 Cat(고양이)의 C예요.
                            🐶 D는 Dog(강아지)의 D예요.
                            🐘 E는 Elephant(코끼리)의 E예요."""),
                    new LessonSeed("알파벳 F부터 J까지", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 다음 알파벳을 만나요!
                            🐟 F는 Fish(물고기)의 F예요.
                            🍇 G는 Grape(포도)의 G예요.
                            🎩 H는 Hat(모자)의 H예요.
                            🍦 I는 Ice cream(아이스크림)의 I예요.
                            🧃 J는 Juice(주스)의 J예요."""),
                    new LessonSeed("알파벳 K부터 O까지", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 알파벳을 더 배워봐요!
                            👑 K는 King(왕)의 K예요.
                            🦁 L은 Lion(사자)의 L이에요.
                            🌙 M은 Moon(달)의 M이에요.
                            🦉 N은 Night owl(올빼미)의 N이에요.
                            🐙 O는 Octopus(문어)의 O예요.""")
            )),
            new CourseSeed("그림으로 배우는 첫 영단어", "그림 카드로 색깔과 사물의 이름을 배우는 어휘 중심 코스입니다.", "🎨",
                    MemberType.CHILD, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("색깔 이름 배우기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 색깔을 배워요!
                            🍎 Red apple. — 빨간 사과.
                            🌤️ Blue sky. — 파란 하늘.
                            🌻 Yellow flower. — 노란 꽃.
                            🌱 Green leaf. — 초록 나뭇잎."""),
                    new LessonSeed("동물 이름 배우기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 동물 친구들을 만나요!
                            🐱 Cat. — 고양이.
                            🐶 Dog. — 강아지.
                            🐰 Rabbit. — 토끼.
                            🐻 Bear. — 곰.""")
            )),
            new CourseSeed("소리 내어 읽는 첫 문장", "짧은 문장을 소리 내어 읽으며 읽기 자신감을 키우는 코스입니다.", "📢",
                    MemberType.CHILD, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("짧은 인사 문장", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 큰 소리로 인사해봐요!
                            👋 Hi! — 안녕!
                            👋 Bye! — 잘 가!
                            🙋 Hello! — 안녕하세요!"""),
                    new LessonSeed("짧은 소개 문장", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 나를 소개해봐요!
                            🙋 I am Tom. — 나는 Tom이에요.
                            🎂 I am seven. — 나는 일곱 살이에요.
                            😊 I am happy. — 나는 행복해요.""")
            )),

            // ---------- CHILD / ELEMENTARY ----------
            new CourseSeed("짧은 이야기로 배우는 영어", "짧은 이야기를 읽고 줄거리를 이해하는 읽기 코스입니다.", "📖",
                    MemberType.CHILD, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("이야기 속 인물 찾기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 이야기를 읽어봐요!
                            🧒 This is Sam. — 이 아이는 Sam이에요.
                            🐕 Sam has a dog. — Sam은 강아지가 있어요.
                            🏡 Sam lives near the park. — Sam은 공원 근처에 살아요."""),
                    new LessonSeed("이야기 순서 이해하기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 이야기 순서를 따라가요!
                            ⏰ First, Sam wakes up. — 먼저 Sam이 일어나요.
                            🍳 Then, Sam eats breakfast. — 그다음 아침을 먹어요.
                            🎒 Finally, Sam goes to school. — 마지막으로 학교에 가요.""")
            )),
            new CourseSeed("동물 이름과 색깔 표현", "동물과 색깔을 조합한 표현을 배우는 어휘 확장 코스입니다.", "🐾",
                    MemberType.CHILD, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("동물 색깔 말하기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 동물 색깔을 말해봐요!
                            🐶 A brown dog. — 갈색 강아지.
                            🐱 A white cat. — 하얀 고양이.
                            🐦 A yellow bird. — 노란 새."""),
                    new LessonSeed("좋아하는 동물 말하기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 좋아하는 동물을 말해봐요!
                            🐰 I like rabbits. — 나는 토끼를 좋아해요.
                            🐱 Do you like cats? — 고양이 좋아해요?
                            🐘 My favorite animal is an elephant. — 제가 좋아하는 동물은 코끼리예요.""")
            )),
            new CourseSeed("학교에서 쓰는 영어 표현", "교실에서 자주 쓰는 표현을 배우는 생활 회화 코스입니다.", "🏫",
                    MemberType.CHILD, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("교실 물건 이름", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 교실 물건 이름을 배워요!
                            ✏️ This is my pencil. — 이건 내 연필이에요.
                            📕 Where is my book? — 내 책이 어디 있지?
                            🎒 This is my backpack. — 이건 내 가방이에요."""),
                    new LessonSeed("선생님께 말하기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 교실 표현을 배워요!
                            🙋 May I go to the bathroom? — 화장실 가도 될까요?
                            🤔 I don't understand. — 이해가 안 돼요.
                            ❓ Can you say that again? — 다시 말씀해 주시겠어요?""")
            )),

            // ---------- CHILD / INTERMEDIATE ----------
            new CourseSeed("짧은 대화문 읽고 이해하기", "두 사람의 짧은 대화를 읽고 내용을 파악하는 코스입니다.", "💬",
                    MemberType.CHILD, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("친구와의 대화", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 친구와의 대화를 읽어봐요!
                            🗨️ A: What's your favorite color? — 제일 좋아하는 색이 뭐야?
                            🗨️ B: I like blue. — 난 파란색이 좋아.
                            🗨️ A: Me too! — 나도!"""),
                    new LessonSeed("가족과의 대화", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 가족과의 대화를 읽어봐요!
                            🗨️ A: Where is Dad? — 아빠 어디 계셔?
                            🗨️ B: He is at work. — 회사에 계셔.
                            🗨️ A: When is he coming home? — 언제 집에 오셔?""")
            )),
            new CourseSeed("일기 쓰기로 배우는 문장 구조", "하루 일과를 문장으로 써보며 문장 구조를 익히는 쓰기 코스입니다.", "📔",
                    MemberType.CHILD, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("오늘 한 일 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 오늘 있었던 일을 써봐요!
                            ⚽ Today, I played soccer. — 오늘 나는 축구를 했어요.
                            😄 It was fun. — 재미있었어요.
                            🍦 After that, I ate ice cream. — 그 후에 아이스크림을 먹었어요."""),
                    new LessonSeed("내일 할 일 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 내일 할 일을 써봐요!
                            👵 Tomorrow, I will visit my grandma. — 내일 할머니 댁에 갈 거예요.
                            🎁 I will bring a gift. — 선물을 가져갈 거예요.""")
            )),
            new CourseSeed("좋아하는 것 소개하기", "취미와 좋아하는 것을 소개하는 말하기·쓰기 코스입니다.", "⭐",
                    MemberType.CHILD, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("취미 소개하기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 취미를 소개해봐요!
                            🎨 My hobby is drawing. — 제 취미는 그림 그리기예요.
                            📅 I draw every day. — 저는 매일 그림을 그려요."""),
                    new LessonSeed("좋아하는 음식 소개하기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 좋아하는 음식을 소개해봐요!
                            🍕 My favorite food is pizza. — 제가 좋아하는 음식은 피자예요.
                            😋 It's so delicious. — 정말 맛있어요.""")
            )),

            // ---------- CHILD / ADVANCED ----------
            new CourseSeed("짧은 영어 동화 읽기", "쉬운 영어 동화를 읽고 교훈을 이해하는 심화 읽기 코스입니다.", "🧚",
                    MemberType.CHILD, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("동화 속 교훈 찾기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 동화 속 교훈을 찾아봐요!
                            🐇 The rabbit was too proud. — 토끼는 너무 자만했어요.
                            🐢 Slow and steady wins the race. — 느려도 꾸준한 게 이겨요."""),
                    new LessonSeed("동화 다시 말하기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 동화를 다시 말해봐요!
                            📖 Once upon a time... — 옛날 옛적에...
                            🏰 There lived a kind princess. — 착한 공주가 살았어요.
                            💫 And they lived happily ever after. — 그리고 그들은 행복하게 살았답니다.""")
            )),
            new CourseSeed("그림책 속 표현 정리하기", "그림책에 나온 표현을 정리하고 활용해보는 코스입니다.", "📚",
                    MemberType.CHILD, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("그림책 표현 모으기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 그림책 표현을 모아봐요!
                            🌩️ It was a dark and stormy night. — 어둡고 폭풍우 치는 밤이었어요.
                            🏚️ Deep in the forest stood an old house. — 숲속 깊은 곳에 낡은 집이 있었어요."""),
                    new LessonSeed("표현 바꿔 말하기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 표현을 바꿔 말해봐요!
                            ☀️ It was a bright and sunny day. — 밝고 화창한 날이었어요.
                            🌳 Near the village stood a tall tree. — 마을 근처에 큰 나무가 있었어요.""")
            )),
            new CourseSeed("나만의 이야기 만들기", "배운 표현을 활용해 짧은 이야기를 직접 만들어보는 코스입니다.", "✍️",
                    MemberType.CHILD, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("이야기 시작 문장 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 이야기를 시작해봐요!
                            🗺️ One day, a boy found a map. — 어느 날, 한 소년이 지도를 발견했어요.
                            🏝️ The map showed a hidden island. — 지도에는 숨겨진 섬이 그려져 있었어요."""),
                    new LessonSeed("이야기 마무리하기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 이야기를 마무리해봐요!
                            🌈 In the end, everyone was happy. — 결국 모두가 행복했어요.
                            🎉 They celebrated together. — 그들은 함께 축하했어요.""")
            )),

            // ---------- ADULT / BEGINNER ----------
            new CourseSeed("왕초보 성인 영어 회화", "인사, 자기소개부터 실생활 표현까지 — 말하기 중심의 입문 코스입니다.", "🗣️",
                    MemberType.ADULT, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("인사와 자기소개", LessonType.VOCAB, """
                            Hello! My name is Jane. — 안녕하세요! 제 이름은 Jane입니다.
                            Nice to meet you. — 만나서 반갑습니다.
                            How are you today? — 오늘 어떠세요?"""),
                    new LessonSeed("숫자와 시간 표현", LessonType.VOCAB, """
                            What time is it? — 지금 몇 시예요?
                            It's three thirty. — 3시 30분이에요.
                            I have an appointment at four. — 4시에 약속이 있어요."""),
                    new LessonSeed("쇼핑할 때 쓰는 표현", LessonType.VOCAB, """
                            How much is this? — 이거 얼마예요?
                            I'll take this one. — 이걸로 할게요.
                            Do you have a smaller size? — 더 작은 사이즈 있나요?"""),
                    new LessonSeed("식당에서 주문하기", LessonType.VOCAB, """
                            Can I get the menu, please? — 메뉴 좀 주시겠어요?
                            I'd like a coffee. — 커피 한 잔 주세요.
                            Can I get the bill, please? — 계산서 주시겠어요?""")
            )),
            new CourseSeed("여행 영어 실전 표현", "공항, 숙소, 식당에서 바로 쓰는 여행 필수 표현 코스입니다.", "✈️",
                    MemberType.ADULT, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("공항에서 쓰는 표현", LessonType.VOCAB, """
                            Where is the gate? — 게이트가 어디예요?
                            I'd like a window seat. — 창가 자리로 주세요.
                            Is this flight on time? — 이 항공편 제시간에 출발하나요?"""),
                    new LessonSeed("숙소에서 쓰는 표현", LessonType.VOCAB, """
                            I have a reservation. — 예약했어요.
                            What time is check-out? — 체크아웃이 몇 시예요?
                            Could I get an extra towel? — 수건 하나 더 주시겠어요?""")
            )),
            new CourseSeed("하루 일과 말하기", "일상적인 하루 일과를 영어로 말해보는 입문 코스입니다.", "⏰",
                    MemberType.ADULT, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("아침 일과 말하기", LessonType.VOCAB, """
                            I wake up at seven. — 저는 7시에 일어나요.
                            I have breakfast. — 아침을 먹어요.
                            I leave home at eight. — 저는 8시에 집을 나서요."""),
                    new LessonSeed("저녁 일과 말하기", LessonType.VOCAB, """
                            I go home at six. — 저는 6시에 집에 가요.
                            I watch TV. — TV를 봐요.
                            I go to bed at eleven. — 저는 11시에 자요.""")
            )),

            // ---------- ADULT / ELEMENTARY ----------
            new CourseSeed("이메일과 메시지 영어 표현", "업무·일상 이메일과 메시지에서 자주 쓰는 표현을 배우는 코스입니다.", "📧",
                    MemberType.ADULT, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("이메일 시작·끝맺음", LessonType.WRITING, """
                            Dear Mr. Kim, — 김 선생님께,
                            I hope this email finds you well. — 잘 지내고 계시길 바랍니다.
                            Best regards, — 감사합니다,"""),
                    new LessonSeed("메시지로 약속 잡기", LessonType.WRITING, """
                            Are you free tomorrow? — 내일 시간 있어요?
                            Let's meet at 3. — 3시에 만나요.
                            Sounds good, see you then. — 좋아요, 그때 봐요.""")
            )),
            new CourseSeed("회의에서 자주 쓰는 표현", "회의 시작부터 마무리까지 쓰이는 표현을 배우는 코스입니다.", "🧑‍💼",
                    MemberType.ADULT, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("회의 시작하기", LessonType.VOCAB, """
                            Let's get started. — 시작하겠습니다.
                            Shall we begin? — 시작할까요?
                            Everyone is here, so let's begin. — 다 모이셨으니 시작하겠습니다."""),
                    new LessonSeed("의견 묻기", LessonType.VOCAB, """
                            What do you think? — 어떻게 생각하세요?
                            Any other opinions? — 다른 의견 있으세요?
                            Let's hear from everyone. — 모두의 의견을 들어봐요.""")
            )),
            new CourseSeed("전화 영어 기초", "전화 통화에서 쓰는 기본 표현을 배우는 코스입니다.", "📞",
                    MemberType.ADULT, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("전화 받고 걸기", LessonType.VOCAB, """
                            May I speak to Mr. Lee? — 이 선생님과 통화할 수 있을까요?
                            Hold on, please. — 잠시만요.
                            He's not available right now. — 지금 자리에 안 계세요."""),
                    new LessonSeed("전화로 약속 정하기", LessonType.VOCAB, """
                            Can we reschedule? — 일정을 조정할 수 있을까요?
                            I'll call you back. — 다시 전화드릴게요.
                            Thanks for calling. — 전화 주셔서 감사합니다.""")
            )),

            // ---------- ADULT / INTERMEDIATE ----------
            new CourseSeed("비즈니스 영어 이메일 작성", "업무 상황별 이메일을 작성해보는 실용 쓰기 코스입니다.", "💼",
                    MemberType.ADULT, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("요청 이메일 쓰기", LessonType.WRITING, """
                            Could you send me the file? — 파일을 보내주실 수 있나요?
                            Please let me know if you need more information. — 추가 정보가 필요하면 알려주세요.
                            Thank you in advance. — 미리 감사드립니다."""),
                    new LessonSeed("사과 이메일 쓰기", LessonType.WRITING, """
                            I apologize for the delay. — 지연에 대해 사과드립니다.
                            This won't happen again. — 다시는 이런 일이 없도록 하겠습니다.
                            Thank you for your understanding. — 양해해 주셔서 감사합니다.""")
            )),
            new CourseSeed("협상과 의견 표현하기", "의견을 정중하게 주고받는 표현을 배우는 코스입니다.", "🤝",
                    MemberType.ADULT, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("동의·반대 표현", LessonType.VOCAB, """
                            I agree with that. — 그 의견에 동의해요.
                            I see it differently. — 저는 다르게 생각해요.
                            That makes sense. — 일리가 있네요."""),
                    new LessonSeed("타협안 제시하기", LessonType.VOCAB, """
                            How about a compromise? — 절충안은 어떨까요?
                            Let's meet halfway. — 서로 조금씩 양보해요.
                            I can agree to that. — 그건 동의할 수 있어요.""")
            )),
            new CourseSeed("프레젠테이션 영어 표현", "발표 시작부터 질의응답까지 쓰는 표현을 배우는 코스입니다.", "📊",
                    MemberType.ADULT, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("발표 시작하기", LessonType.VOCAB, """
                            Today, I'll talk about... — 오늘은 ...에 대해 말씀드리겠습니다.
                            Let me start with an overview. — 개요부터 말씀드리겠습니다."""),
                    new LessonSeed("질문 받기", LessonType.VOCAB, """
                            Any questions so far? — 지금까지 질문 있으신가요?
                            That's a great question. — 좋은 질문이네요.
                            Let me get back to you on that. — 그 부분은 다시 확인해서 알려드릴게요.""")
            )),

            // ---------- ADULT / ADVANCED ----------
            new CourseSeed("시사 이슈로 배우는 영어 토론", "시사 주제로 의견을 주고받는 토론 표현을 배우는 코스입니다.", "🗞️",
                    MemberType.ADULT, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("주장 펼치기", LessonType.VOCAB, """
                            In my opinion, — 제 생각에는,
                            The evidence suggests that... — 증거에 따르면...
                            This is a critical issue. — 이것은 중요한 사안입니다."""),
                    new LessonSeed("반박하기", LessonType.VOCAB, """
                            That's a fair point, but... — 타당한 지적이지만...
                            I'd like to add that... — 덧붙이자면...
                            On the other hand, — 반면에,""")
            )),
            new CourseSeed("원서로 읽는 짧은 에세이", "짧은 영어 에세이를 읽고 핵심을 파악하는 심화 읽기 코스입니다.", "📜",
                    MemberType.ADULT, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("주제문 찾기", LessonType.READING, """
                            The main idea of this essay is... — 이 글의 핵심은...
                            The author begins by describing... — 글쓴이는 ...을 설명하며 시작한다."""),
                    new LessonSeed("글쓴이 의도 파악하기", LessonType.READING, """
                            The author suggests that... — 글쓴이는 ...라고 제안한다.
                            This implies that... — 이것은 ...을 암시한다.""")
            )),
            new CourseSeed("인터뷰 영어 표현 정리", "면접과 인터뷰에서 쓰는 표현을 정리하는 실전 코스입니다.", "🎤",
                    MemberType.ADULT, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("자기소개하기", LessonType.VOCAB, """
                            Let me introduce myself. — 제 소개를 하겠습니다.
                            I have three years of experience in this field. — 이 분야에서 3년의 경력이 있습니다."""),
                    new LessonSeed("강점 말하기", LessonType.VOCAB, """
                            My strength is... — 저의 강점은...
                            I'm confident in my ability to... — 저는 ...하는 능력에 자신이 있습니다.""")
            ))
    );
}
