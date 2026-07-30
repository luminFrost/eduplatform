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
 * 코스마다 7개 안팎의 레슨을 채워 "레슨이 너무 적다"는 피드백을 반영했다.
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
                            🐙 O는 Octopus(문어)의 O예요."""),
                    new LessonSeed("알파벳 P부터 T까지", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 알파벳 P부터 T까지 배워요!
                            🐼 P는 Panda(팬더)의 P예요.
                            👑 Q는 Queen(여왕)의 Q예요.
                            🐰 R는 Rabbit(토끼)의 R예요.
                            ☀️ S는 Sun(해)의 S예요.
                            🐯 T는 Tiger(호랑이)의 T예요."""),
                    new LessonSeed("알파벳 U부터 Y까지", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 알파벳 U부터 Y까지 배워요!
                            ☂️ U는 Umbrella(우산)의 U예요.
                            🎻 V는 Violin(바이올린)의 V예요.
                            🍉 W는 Watermelon(수박)의 W예요.
                            🩻 X는 X-ray(엑스레이)의 X예요.
                            🪀 Y는 Yo-yo(요요)의 Y예요."""),
                    new LessonSeed("알파벳 Z 파헤치기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 마지막 알파벳 Z를 배워요!
                            🦓 Z는 Zebra(얼룩말)의 Z예요.
                            🦁 Z는 Zoo(동물원)의 Z예요.
                            0️⃣ Z는 Zero(영)의 Z예요.
                            ⚡ Z는 Zigzag(지그재그)의 Z예요."""),
                    new LessonSeed("알파벳 A부터 E까지 복습", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 알파벳을 다시 복습해요!
                            🐜 A는 Ant(개미)의 A예요.
                            🐻 B는 Bear(곰)의 B예요.
                            🚗 C는 Car(자동차)의 C예요.
                            🦆 D는 Duck(오리)의 D예요.
                            🥚 E는 Egg(달걀)의 E예요.""")
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
                            🐻 Bear. — 곰."""),
                    new LessonSeed("숫자 이름 배우기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 숫자를 배워요!
                            1️⃣ One. — 하나.
                            2️⃣ Two. — 둘.
                            3️⃣ Three. — 셋.
                            4️⃣ Four. — 넷.
                            5️⃣ Five. — 다섯."""),
                    new LessonSeed("가족 이름 배우기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 가족 이름을 배워요!
                            👩 Mom. — 엄마.
                            👨 Dad. — 아빠.
                            👧 Sister. — 여동생.
                            👦 Brother. — 남동생.
                            👶 Baby. — 아기."""),
                    new LessonSeed("음식 이름 배우기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 음식 이름을 배워요!
                            🍚 Rice. — 밥.
                            🥛 Milk. — 우유.
                            🍞 Bread. — 빵.
                            🥚 Egg. — 달걀.
                            🍪 Cookie. — 쿠키."""),
                    new LessonSeed("몸 이름 배우기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 몸 이름을 배워요!
                            👀 Eyes. — 눈.
                            👃 Nose. — 코.
                            👄 Mouth. — 입.
                            🖐️ Hands. — 손.
                            🦶 Feet. — 발."""),
                    new LessonSeed("날씨 표현 배우기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 날씨 표현을 배워요!
                            ☀️ Sunny day. — 맑은 날.
                            🌧️ Rainy day. — 비 오는 날.
                            ❄️ Snowy day. — 눈 오는 날.
                            💨 Windy day. — 바람 부는 날.""")
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
                            😊 I am happy. — 나는 행복해요."""),
                    new LessonSeed("고마움과 미안함 표현", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 고마움과 미안함을 표현해요!
                            🙏 Thank you! — 고마워요!
                            😊 You're welcome. — 천만에요.
                            😢 I'm sorry. — 미안해요.
                            👍 That's okay. — 괜찮아요."""),
                    new LessonSeed("가족에게 하는 인사", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 가족에게 인사해요!
                            🌞 Good morning, Mom! — 엄마, 좋은 아침이에요!
                            🌙 Good night, Dad! — 아빠, 안녕히 주무세요!
                            ❤️ I love you. — 사랑해요."""),
                    new LessonSeed("간단한 질문 문장", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 궁금한 것을 물어봐요!
                            ❓ What is this? — 이게 뭐예요?
                            🎒 Where is my bag? — 내 가방이 어디 있어요?
                            🙋 Who are you? — 누구세요?"""),
                    new LessonSeed("기분을 말하는 문장", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 내 기분을 말해요!
                            😢 I am sad. — 나는 슬퍼요.
                            😴 I am tired. — 나는 피곤해요.
                            🤩 I am excited. — 나는 신나요.
                            😱 I am scared. — 나는 무서워요."""),
                    new LessonSeed("숫자 세며 말하기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 숫자를 세며 말해요!
                            🔢 Let's count! — 같이 세어 봐요!
                            🖐️ One, two, three! — 하나, 둘, 셋!
                            ✋ I have five fingers. — 나는 손가락이 다섯 개예요.""")
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
                            🎒 Finally, Sam goes to school. — 마지막으로 학교에 가요."""),
                    new LessonSeed("Sam의 하루 시작하기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 Sam의 아침을 만나요!
                            ⏰ Sam wakes up early. — Sam은 일찍 일어나요.
                            🪥 Sam brushes his teeth. — Sam은 이를 닦아요.
                            👕 Sam gets dressed. — Sam은 옷을 입어요."""),
                    new LessonSeed("Sam의 학교 가는 길", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 Sam의 등굣길을 따라가요!
                            🚪 Sam leaves home. — Sam은 집을 나서요.
                            🚶 Sam walks to school. — Sam은 학교까지 걸어가요.
                            🤝 Sam meets his friend. — Sam은 친구를 만나요."""),
                    new LessonSeed("Sam과 친구 Amy", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 Sam의 친구를 만나요!
                            👧 Sam has a friend. — Sam에게는 친구가 있어요.
                            💬 Her name is Amy. — 그 친구의 이름은 Amy예요.
                            🎈 They play together. — 둘은 함께 놀아요."""),
                    new LessonSeed("Sam의 저녁 시간", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 Sam의 저녁 시간을 살펴봐요!
                            🏠 Sam comes home. — Sam은 집에 돌아와요.
                            🍽️ Sam eats dinner. — Sam은 저녁을 먹어요.
                            📖 Sam reads a book. — Sam은 책을 읽어요."""),
                    new LessonSeed("Sam의 즐거운 주말", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 Sam의 신나는 주말을 만나요!
                            📅 It is Saturday. — 토요일이에요.
                            🌳 Sam goes to the park. — Sam은 공원에 가요.
                            😊 Sam feels happy. — Sam은 행복해요.""")
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
                            🐘 My favorite animal is an elephant. — 제가 좋아하는 동물은 코끼리예요."""),
                    new LessonSeed("동물 크기 표현하기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 동물의 크기를 표현해요!
                            🐘 A big elephant. — 큰 코끼리.
                            🐭 A small mouse. — 작은 쥐.
                            🦒 A tall giraffe. — 키 큰 기린."""),
                    new LessonSeed("동물 울음소리 표현", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 동물 울음소리를 배워요!
                            🐶 A dog says woof. — 강아지는 멍멍 울어요.
                            🐱 A cat says meow. — 고양이는 야옹 울어요.
                            🐮 A cow says moo. — 소는 음매 울어요."""),
                    new LessonSeed("동물이 사는 곳", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 동물이 사는 곳을 알아봐요!
                            🐟 Fish live in water. — 물고기는 물에 살아요.
                            🐦 Birds live in trees. — 새는 나무에 살아요.
                            🐻 Bears live in the forest. — 곰은 숲에 살아요."""),
                    new LessonSeed("더 다양한 동물 색깔", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 더 다양한 동물 색깔을 배워요!
                            🐷 A pink pig. — 분홍 돼지.
                            🐺 A gray wolf. — 회색 늑대.
                            🦊 An orange fox. — 주황 여우."""),
                    new LessonSeed("동물이 좋아하는 먹이", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 동물이 좋아하는 먹이를 배워요!
                            🐰 Rabbits like carrots. — 토끼는 당근을 좋아해요.
                            🐵 Monkeys like bananas. — 원숭이는 바나나를 좋아해요.
                            🐼 Pandas like bamboo. — 판다는 대나무를 좋아해요.""")
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
                            ❓ Can you say that again? — 다시 말씀해 주시겠어요?"""),
                    new LessonSeed("도움을 요청하는 말", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 도움을 요청하는 말을 배워요!
                            🙋 Can you help me? — 도와주시겠어요?
                            ✏️ I need a pencil. — 연필이 필요해요.
                            🖊️ I don't have a pen. — 펜이 없어요."""),
                    new LessonSeed("쉬는 시간에 하는 말", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 쉬는 시간 표현을 배워요!
                            ⏰ It's recess time! — 쉬는 시간이에요!
                            🏃 Let's play outside. — 밖에서 놀자.
                            🤝 Let's play together. — 같이 놀자."""),
                    new LessonSeed("점심시간에 하는 말", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 점심시간 표현을 배워요!
                            🍽️ It's lunchtime! — 점심시간이에요!
                            🍚 I'm hungry. — 배고파요.
                            😋 This is delicious. — 이거 맛있어요."""),
                    new LessonSeed("친구와 나누는 말", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 친구와 나누는 말을 배워요!
                            🧑‍🤝‍🧑 You are my friend. — 너는 내 친구야.
                            🤗 Let's be friends. — 우리 친구하자.
                            🙋‍♀️ Can I join you? — 나도 같이 해도 돼?"""),
                    new LessonSeed("숙제에 대해 말하기", LessonType.VOCAB, """
                            INTRO: 🦊 여우 선생님과 함께 숙제에 대해 말해요!
                            📚 Do we have homework? — 숙제 있어요?
                            ✅ I finished my homework. — 숙제 다 했어요.
                            😅 I forgot my homework. — 숙제를 깜빡했어요.""")
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
                            🗨️ A: When is he coming home? — 언제 집에 오셔?"""),
                    new LessonSeed("학교 생활 대화하기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 학교 이야기를 나눠 봐요!
                            🏫 A: How was school today? — 오늘 학교 어땠어?
                            📚 B: It was great. We had an art class. — 좋았어. 미술 수업이 있었어.
                            😊 A: That sounds fun! — 재미있었겠다!"""),
                    new LessonSeed("주말 계획 이야기하기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 주말 계획을 말해봐요!
                            📅 A: What are you doing this weekend? — 이번 주말에 뭐 할 거야?
                            🚲 B: I'm going to ride my bike with my brother. — 형이랑 자전거 탈 거야.
                            🎉 A: That sounds like fun! — 재밌겠다!"""),
                    new LessonSeed("날씨에 대해 대화하기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 날씨 이야기를 해봐요!
                            🌦️ A: How's the weather today? — 오늘 날씨 어때?
                            ☔ B: It's raining, so bring an umbrella. — 비가 오니까 우산 챙겨.
                            🧥 A: Okay, I'll wear my raincoat too. — 알겠어, 우비도 입을게."""),
                    new LessonSeed("가게에서 나누는 대화", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 가게에서 대화해봐요!
                            🛍️ A: Excuse me, how much is this shirt? — 저기요, 이 셔츠 얼마예요?
                            💰 B: It's ten dollars. — 10달러예요.
                            🙂 A: Okay, I'll take it. — 네, 이걸로 살게요."""),
                    new LessonSeed("취미에 대해 이야기 나누기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 취미 이야기를 나눠봐요!
                            🎸 A: What do you do in your free time? — 여가 시간에 뭐 해?
                            🎮 B: I usually play video games with my friends. — 보통 친구들이랑 게임을 해.
                            😄 A: That sounds like a lot of fun! — 정말 재밌겠다!""")
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
                            🎁 I will bring a gift. — 선물을 가져갈 거예요."""),
                    new LessonSeed("주말 여행 일기 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 주말 여행 일기를 써봐요!
                            🚗 Last weekend, I went to the beach with my family. — 지난 주말에 가족과 바다에 갔어요.
                            🏖️ We built a big sandcastle together. — 우리는 함께 큰 모래성을 만들었어요.
                            😊 It was one of my best days. — 최고의 하루였어요."""),
                    new LessonSeed("생일 파티 일기 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 생일 파티 일기를 써봐요!
                            🎂 Yesterday was my friend's birthday party. — 어제는 친구의 생일 파티였어요.
                            🎈 We sang songs and ate cake together. — 우리는 함께 노래를 부르고 케이크를 먹었어요.
                            🎁 I gave her a nice present. — 저는 그녀에게 멋진 선물을 줬어요."""),
                    new LessonSeed("운동회 일기 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 운동회 일기를 써봐요!
                            🏃 Today was our school sports day. — 오늘은 학교 운동회 날이었어요.
                            🥇 I ran very fast and won first place. — 저는 아주 빨리 달려서 1등을 했어요.
                            👏 My friends cheered loudly for me. — 친구들이 저를 위해 크게 응원해줬어요."""),
                    new LessonSeed("비 오는 날 일기 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 비 오는 날 일기를 써봐요!
                            🌧️ It rained all day yesterday. — 어제는 하루 종일 비가 왔어요.
                            📖 So I stayed home and read books. — 그래서 저는 집에서 책을 읽었어요.
                            ☕ I also drank warm tea with my mom. — 엄마와 따뜻한 차도 마셨어요."""),
                    new LessonSeed("친구 집 방문 계획 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 친구 집 방문 계획을 써봐요!
                            🚪 This Saturday, I will visit my friend's house. — 이번 토요일에 친구 집에 갈 거예요.
                            🧩 We will play board games together. — 우리는 함께 보드게임을 할 거예요.
                            🍪 I will bring some cookies for her. — 그녀에게 줄 쿠키도 가져갈 거예요.""")
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
                            😋 It's so delicious. — 정말 맛있어요."""),
                    new LessonSeed("좋아하는 계절 소개하기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 좋아하는 계절을 소개해봐요!
                            🍂 My favorite season is fall. — 제가 좋아하는 계절은 가을이에요.
                            🍁 The leaves turn red and yellow. — 나뭇잎이 빨갛고 노랗게 변해요.
                            🌤️ The weather is cool and nice. — 날씨가 시원하고 좋아요."""),
                    new LessonSeed("좋아하는 과목 소개하기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 좋아하는 과목을 소개해봐요!
                            🔬 My favorite subject is science. — 제가 좋아하는 과목은 과학이에요.
                            🧪 I like doing fun experiments in class. — 저는 수업 시간에 재미있는 실험하는 걸 좋아해요.
                            😃 It makes me curious about the world. — 그것은 저를 세상에 대해 궁금하게 만들어요."""),
                    new LessonSeed("좋아하는 게임 소개하기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 좋아하는 게임을 소개해봐요!
                            🎮 My favorite game is a puzzle game. — 제가 좋아하는 게임은 퍼즐 게임이에요.
                            🧠 It makes my brain work hard. — 그것은 제 머리를 많이 쓰게 해요.
                            🏆 I feel proud when I solve it. — 문제를 풀면 뿌듯해요."""),
                    new LessonSeed("좋아하는 책 소개하기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 좋아하는 책을 소개해봐요!
                            📗 My favorite book is about a brave girl. — 제가 좋아하는 책은 용감한 소녀에 관한 이야기예요.
                            🐉 She goes on an adventure to save her village. — 그녀는 마을을 구하기 위해 모험을 떠나요.
                            ❤️ I read it again and again. — 저는 그 책을 계속해서 읽어요."""),
                    new LessonSeed("좋아하는 장소 소개하기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 좋아하는 장소를 소개해봐요!
                            🏞️ My favorite place is the park near my house. — 제가 좋아하는 장소는 집 근처 공원이에요.
                            🌳 I can see tall trees and a small pond there. — 그곳에서 큰 나무들과 작은 연못을 볼 수 있어요.
                            🧺 I often have a picnic there with my family. — 저는 종종 가족과 그곳에서 소풍을 해요.""")
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
                            💫 And they lived happily ever after. — 그리고 그들은 행복하게 살았답니다."""),
                    new LessonSeed("개미와 베짱이 이야기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 개미 이야기를 읽어봐요!
                            🐜 The ant worked hard all summer long. — 개미는 여름 내내 열심히 일했어요.
                            🎶 The grasshopper only sang and played. — 베짱이는 노래하고 놀기만 했어요.
                            ❄️ When winter came, the ant had plenty of food. — 겨울이 오자 개미는 먹을 것이 충분했어요."""),
                    new LessonSeed("양치기 소년 이야기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 양치기 소년 이야기를 읽어봐요!
                            🐑 A boy shouted, "A wolf is coming!" — 한 소년이 "늑대가 온다!"라고 외쳤어요.
                            😅 But it was only a joke, and everyone laughed. — 하지만 그건 장난이었고, 모두 웃었어요.
                            😢 Later, a real wolf came, but no one believed him. — 나중에 진짜 늑대가 왔지만 아무도 그를 믿지 않았어요."""),
                    new LessonSeed("여우와 포도 이야기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 포도밭 이야기를 읽어봐요!
                            🍇 A hungry fox saw juicy grapes on a tall vine. — 배고픈 여우가 높은 넝쿨에 달린 즙 많은 포도를 봤어요.
                            🦶 He jumped again and again, but he could not reach them. — 여우는 계속 뛰었지만 포도에 닿을 수 없었어요.
                            🙄 "Those grapes are probably sour anyway," he said and walked away. — "저 포도는 어차피 실 거야"라고 말하며 그는 떠났어요."""),
                    new LessonSeed("사자와 생쥐 이야기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 사자와 생쥐 이야기를 읽어봐요!
                            🦁 A small mouse woke a sleeping lion by accident. — 작은 생쥐가 실수로 자고 있던 사자를 깨웠어요.
                            🙏 The lion let the mouse go instead of eating it. — 사자는 생쥐를 잡아먹지 않고 놓아줬어요.
                            🕸️ Later, the mouse chewed a net and set the lion free. — 나중에 생쥐는 그물을 갉아 사자를 구해줬어요."""),
                    new LessonSeed("도시 쥐와 시골 쥐 이야기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 도시 쥐 이야기를 읽어봐요!
                            🏙️ The country mouse visited his cousin in the big city. — 시골 쥐가 도시에 사는 사촌을 찾아갔어요.
                            😰 The city had fancy food, but also loud noises and danger. — 도시에는 좋은 음식이 있었지만 시끄럽고 위험하기도 했어요.
                            🌾 The country mouse decided his quiet home was better for him. — 시골 쥐는 조용한 자기 집이 더 좋다고 생각했어요.""")
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
                            🌳 Near the village stood a tall tree. — 마을 근처에 큰 나무가 있었어요."""),
                    new LessonSeed("눈 덮인 산 표현 모으기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 눈 덮인 산 이야기를 읽어봐요!
                            🏔️ High on the snowy mountain, the wind blew softly. — 눈 덮인 높은 산에서 바람이 부드럽게 불었어요.
                            ⛄ A little cabin stood alone under the falling snow. — 작은 오두막이 내리는 눈 아래 홀로 서 있었어요."""),
                    new LessonSeed("분주한 도시 거리 표현 모으기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 분주한 도시 이야기를 읽어봐요!
                            🏙️ The city street was full of busy people and bright lights. — 도시 거리는 바쁜 사람들과 밝은 불빛으로 가득했어요.
                            🚕 Yellow taxis honked as they passed by quickly. — 노란 택시들이 빠르게 지나가며 경적을 울렸어요."""),
                    new LessonSeed("조용한 도서관 표현 모으기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 조용한 도서관 이야기를 읽어봐요!
                            📚 Inside the quiet library, no one made a sound. — 조용한 도서관 안에서는 아무도 소리를 내지 않았어요.
                            🕯️ Soft light fell over the old wooden shelves. — 부드러운 빛이 오래된 나무 책장 위로 비쳤어요."""),
                    new LessonSeed("신비한 정원 표현 모으기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 신비한 정원 이야기를 읽어봐요!
                            🌸 In the magical garden, flowers glowed like tiny stars. — 신비한 정원에서는 꽃들이 작은 별처럼 빛났어요.
                            🦋 Colorful butterflies danced between the singing trees. — 알록달록한 나비들이 노래하는 나무들 사이에서 춤을 췄어요."""),
                    new LessonSeed("깊은 바닷속 표현 모으기", LessonType.READING, """
                            INTRO: 🦊 여우 선생님과 함께 깊은 바닷속 이야기를 읽어봐요!
                            🌊 Deep in the sea, everything was calm and blue. — 깊은 바닷속은 모든 것이 고요하고 파랬어요.
                            🐠 Strange, glowing fish swam slowly past ancient rocks. — 신기하게 빛나는 물고기들이 오래된 바위들을 지나 천천히 헤엄쳤어요.""")
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
                            🎉 They celebrated together. — 그들은 함께 축하했어요."""),
                    new LessonSeed("길 잃은 강아지 이야기 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 강아지 이야기를 만들어봐요!
                            🐶 One rainy afternoon, a girl found a lost puppy. — 어느 비 오는 오후, 한 소녀가 길 잃은 강아지를 발견했어요.
                            🏡 She decided to help it find its way home. — 그녀는 강아지가 집을 찾도록 도와주기로 했어요."""),
                    new LessonSeed("마법 열쇠 이야기 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 마법 열쇠 이야기를 만들어봐요!
                            🗝️ A boy found a golden key under an old tree. — 한 소년이 오래된 나무 아래에서 황금 열쇠를 발견했어요.
                            🚪 The key opened a door that led to another world. — 그 열쇠는 다른 세상으로 이어지는 문을 열었어요."""),
                    new LessonSeed("말하는 고양이 이야기 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 말하는 고양이 이야기를 만들어봐요!
                            🐱 One night, a cat suddenly began to talk to me. — 어느 밤, 고양이가 갑자기 저에게 말을 걸기 시작했어요.
                            🌙 It said it needed my help to find a magic star. — 고양이는 마법의 별을 찾는 데 제 도움이 필요하다고 했어요."""),
                    new LessonSeed("빗속 모험 이야기 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 빗속 모험 이야기를 만들어봐요!
                            ⛈️ During a big storm, two friends got lost in the woods. — 큰 폭풍우 속에서 두 친구가 숲에서 길을 잃었어요.
                            🌈 After the rain stopped, they found a beautiful rainbow bridge. — 비가 그친 후, 그들은 아름다운 무지개 다리를 발견했어요."""),
                    new LessonSeed("새로운 친구 이야기 쓰기", LessonType.WRITING, """
                            INTRO: 🦊 여우 선생님과 함께 새로운 친구 이야기를 만들어봐요!
                            🚌 A new student from another town joined my class. — 다른 마을에서 온 새로운 학생이 우리 반에 들어왔어요.
                            🤝 We became best friends after sharing our favorite stories. — 우리는 좋아하는 이야기를 나누며 가장 친한 친구가 되었어요.""")
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
                            Can I get the bill, please? — 계산서 주시겠어요?"""),
                    new LessonSeed("길 묻기", LessonType.VOCAB, """
                            Excuse me, how do I get to the station? — 실례합니다, 역까지 어떻게 가나요?
                            Go straight and turn left. — 직진해서 왼쪽으로 도세요.
                            Is it far from here? — 여기서 먼가요?"""),
                    new LessonSeed("날씨 이야기하기", LessonType.VOCAB, """
                            It's really hot today. — 오늘 정말 덥네요.
                            I think it's going to rain. — 비가 올 것 같아요.
                            I like sunny days. — 저는 맑은 날이 좋아요."""),
                    new LessonSeed("가벼운 대화 나누기", LessonType.VOCAB, """
                            How was your weekend? — 주말 어떻게 보내셨어요?
                            I just stayed home and rested. — 그냥 집에서 쉬었어요.
                            That sounds nice. — 그거 좋네요.""")
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
                            Could I get an extra towel? — 수건 하나 더 주시겠어요?"""),
                    new LessonSeed("택시와 대중교통 이용하기", LessonType.VOCAB, """
                            Can you take me to this address? — 이 주소로 데려다 주시겠어요?
                            How much is the fare? — 요금이 얼마예요?
                            Which bus goes downtown? — 어떤 버스가 시내로 가나요?"""),
                    new LessonSeed("여행 중 식당 이용하기", LessonType.VOCAB, """
                            Do you have an English menu? — 영어 메뉴 있나요?
                            I'm allergic to peanuts. — 저는 땅콩 알레르기가 있어요.
                            Could we get a table for two? — 두 명 자리 있을까요?"""),
                    new LessonSeed("길 물어보기", LessonType.VOCAB, """
                            Excuse me, where is the nearest subway station? — 실례합니다, 가장 가까운 지하철역이 어디예요?
                            How long does it take to walk there? — 거기까지 걸어서 얼마나 걸려요?
                            Can you show me on the map? — 지도에서 보여주시겠어요?"""),
                    new LessonSeed("위급 상황과 분실물 대처하기", LessonType.VOCAB, """
                            I lost my passport. — 여권을 잃어버렸어요.
                            Can you help me, please? — 도와주시겠어요?
                            Where is the nearest police station? — 가장 가까운 경찰서가 어디예요?"""),
                    new LessonSeed("관광하기", LessonType.VOCAB, """
                            What time does the museum open? — 박물관이 몇 시에 열어요?
                            Can I take pictures here? — 여기서 사진 찍어도 되나요?
                            Is there a guided tour available? — 가이드 투어가 있나요?""")
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
                            I go to bed at eleven. — 저는 11시에 자요."""),
                    new LessonSeed("점심시간 이야기하기", LessonType.VOCAB, """
                            I have lunch at noon. — 저는 정오에 점심을 먹어요.
                            I usually eat with my coworkers. — 보통 동료들과 함께 먹어요.
                            I take a short walk after lunch. — 점심 후에 짧게 산책해요."""),
                    new LessonSeed("출퇴근 이야기하기", LessonType.VOCAB, """
                            I take the subway to work. — 저는 지하철을 타고 출근해요.
                            It takes about an hour. — 한 시간 정도 걸려요.
                            I usually listen to music on the way. — 가는 길에 보통 음악을 들어요."""),
                    new LessonSeed("운동 이야기하기", LessonType.VOCAB, """
                            I go to the gym after work. — 저는 퇴근 후에 헬스장에 가요.
                            I exercise three times a week. — 일주일에 세 번 운동해요.
                            I feel great after working out. — 운동 후에는 기분이 좋아요."""),
                    new LessonSeed("주말 일과 말하기", LessonType.VOCAB, """
                            I sleep in on weekends. — 저는 주말에 늦잠을 자요.
                            I meet my friends for brunch. — 친구들과 브런치를 먹어요.
                            I clean my house on Sundays. — 일요일에는 집을 청소해요."""),
                    new LessonSeed("저녁에 쉬는 시간", LessonType.VOCAB, """
                            I relax on the sofa in the evening. — 저는 저녁에 소파에서 쉬어요.
                            I read a book before bed. — 자기 전에 책을 읽어요.
                            I take a warm shower at night. — 밤에 따뜻한 샤워를 해요.""")
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
                            Sounds good, see you then. — 좋아요, 그때 봐요."""),
                    new LessonSeed("회의 확인 이메일 쓰기", LessonType.WRITING, """
                            I'm writing to confirm our meeting. — 회의를 확인하고자 메일 드립니다.
                            The meeting is scheduled for 10 a.m. — 회의는 오전 10시로 예정되어 있습니다.
                            Please let me know if the time works for you. — 시간이 괜찮으신지 알려주세요."""),
                    new LessonSeed("후속 이메일 보내기", LessonType.WRITING, """
                            I'm following up on my previous email. — 지난 이메일에 대해 다시 연락드립니다.
                            Just checking if you had a chance to review this. — 검토하실 시간이 있으셨는지 확인차 연락드립니다.
                            Please let me know if you need more information. — 추가 정보가 필요하시면 알려주세요."""),
                    new LessonSeed("감사 인사 전하기", LessonType.WRITING, """
                            Thank you for your quick response. — 빠른 답변 감사합니다.
                            I really appreciate your help. — 도와주셔서 정말 감사합니다.
                            Thanks again for your time. — 시간 내주셔서 다시 한번 감사드립니다."""),
                    new LessonSeed("이메일로 질문하기", LessonType.WRITING, """
                            I have a quick question about the report. — 보고서에 대해 간단한 질문이 있습니다.
                            Could you clarify this part? — 이 부분을 명확히 해주시겠어요?
                            Please let me know when you're available. — 가능하신 시간을 알려주세요."""),
                    new LessonSeed("편하게 문자 주고받기", LessonType.WRITING, """
                            Hey, what's up? — 안녕, 요즘 어때?
                            I'm running a bit late. — 저 좀 늦을 것 같아요.
                            Talk to you later! — 이따 얘기해요!""")
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
                            Let's hear from everyone. — 모두의 의견을 들어봐요."""),
                    new LessonSeed("결정 사항 정리하기", LessonType.VOCAB, """
                            Let's summarize what we decided. — 결정된 사항을 정리해 봅시다.
                            So, we agreed to move forward with this plan. — 그럼 이 계획으로 진행하기로 합의했습니다.
                            I'll send a summary after the meeting. — 회의 후에 요약본을 보내드릴게요."""),
                    new LessonSeed("다음 회의 일정 잡기", LessonType.VOCAB, """
                            When should we meet again? — 언제 다시 만날까요?
                            Let's schedule the next meeting for Friday. — 다음 회의는 금요일로 잡죠.
                            I'll send a calendar invite. — 캘린더 초대를 보내드릴게요."""),
                    new LessonSeed("정중하게 반대 의견 말하기", LessonType.VOCAB, """
                            I see your point, but I have a different idea. — 이해는 하지만 저는 다른 생각이 있어요.
                            I'm not sure I fully agree. — 완전히 동의하는 건 아니에요.
                            Can we consider another option? — 다른 방안을 고려해볼 수 있을까요?"""),
                    new LessonSeed("회의 안건 소개하기", LessonType.VOCAB, """
                            Today we have three items on the agenda. — 오늘 안건은 세 가지입니다.
                            Let's start with the first item. — 첫 번째 안건부터 시작하죠.
                            We'll move on to the next topic after this. — 이후에 다음 주제로 넘어가겠습니다."""),
                    new LessonSeed("회의 마무리하기", LessonType.VOCAB, """
                            Let's wrap up for today. — 오늘은 여기서 마무리하죠.
                            Thank you all for your time. — 시간 내주셔서 모두 감사합니다.
                            That's all for today's meeting. — 오늘 회의는 여기까지입니다.""")
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
                            Thanks for calling. — 전화 주셔서 감사합니다."""),
                    new LessonSeed("음성 메시지 남기기", LessonType.VOCAB, """
                            Please leave a message after the beep. — 삐 소리 후에 메시지를 남겨주세요.
                            This is John calling for Mr. Park. — 박 선생님께 전화드린 John입니다.
                            Please call me back when you get this. — 이 메시지 받으시면 전화 주세요."""),
                    new LessonSeed("대신 메모 남기기", LessonType.VOCAB, """
                            Can I take a message? — 메모를 남겨드릴까요?
                            I'll let her know you called. — 전화하셨다고 전해드릴게요.
                            Could you tell him to call me back? — 저에게 다시 전화해 달라고 전해주시겠어요?"""),
                    new LessonSeed("전화 건 사람 확인하기", LessonType.VOCAB, """
                            Who's calling, please? — 누구시라고 전해드릴까요?
                            May I ask who is speaking? — 누구신지 여쭤봐도 될까요?
                            Can I ask what this is regarding? — 무슨 일로 전화하셨는지 여쭤봐도 될까요?"""),
                    new LessonSeed("전화 정중하게 끊기", LessonType.VOCAB, """
                            It was nice talking to you. — 이야기 나눠서 좋았습니다.
                            I have to go now. — 이제 가봐야 할 것 같아요.
                            Thanks for calling, have a good day. — 전화 주셔서 감사합니다, 좋은 하루 되세요."""),
                    new LessonSeed("연결 상태가 안 좋을 때", LessonType.VOCAB, """
                            Sorry, I can't hear you well. — 죄송한데 잘 안 들려요.
                            Can you speak up a little? — 조금 더 크게 말씀해 주시겠어요?
                            The line is breaking up. — 전화가 자꾸 끊겨요.""")
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
                            Thank you for your understanding. — 양해해 주셔서 감사합니다."""),
                    new LessonSeed("회의 일정 조율하기", LessonType.WRITING, """
                            Would it be possible to schedule a meeting sometime next week? — 다음 주 중에 회의 일정을 잡을 수 있을까요?
                            I'm available on Tuesday afternoon or Thursday morning. — 저는 화요일 오후나 목요일 오전에 시간이 됩니다.
                            Please let me know what time works best for you. — 어느 시간이 가장 편하신지 알려주세요."""),
                    new LessonSeed("진행 상황 보고하기", LessonType.WRITING, """
                            I'm writing to update you on the current status of the project. — 프로젝트의 현재 진행 상황을 알려드리고자 메일을 씁니다.
                            We are on track to complete the first phase by the end of this month. — 이번 달 말까지 1단계를 완료할 예정입니다.
                            I'll share a more detailed report once the testing phase is finished. — 테스트 단계가 끝나면 더 자세한 보고서를 공유해 드리겠습니다."""),
                    new LessonSeed("마감일 협상하기", LessonType.WRITING, """
                            I'm afraid the current deadline may not be feasible given the scope of the work. — 작업 범위를 고려할 때 현재 마감일은 어려울 것 같습니다.
                            Would it be possible to extend the deadline by one week? — 마감일을 일주일 연장할 수 있을까요?
                            We appreciate your flexibility on this matter. — 이 사안에 대한 유연한 대응에 감사드립니다."""),
                    new LessonSeed("신규 고객에게 자기소개하기", LessonType.WRITING, """
                            I'm reaching out to introduce myself as your new account manager. — 귀하의 신임 담당자로서 제 소개를 드리고자 연락드립니다.
                            I look forward to working closely with you and your team. — 귀하 및 팀과 긴밀히 협력하기를 기대합니다.
                            Please feel free to reach out if you have any questions. — 질문이 있으시면 언제든지 편하게 연락 주세요."""),
                    new LessonSeed("거래 성사 후 감사 인사하기", LessonType.WRITING, """
                            I'm delighted to confirm that we have finalized the agreement. — 계약이 최종 성사되었음을 알려드리게 되어 기쁩니다.
                            Thank you for your trust and continued partnership. — 신뢰와 지속적인 협력에 감사드립니다.
                            We look forward to a successful collaboration ahead. — 앞으로의 성공적인 협업을 기대합니다.""")
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
                            I can agree to that. — 그건 동의할 수 있어요."""),
                    new LessonSeed("대안 제시하기", LessonType.VOCAB, """
                            Perhaps we could consider a different approach. — 다른 접근 방식을 고려해 볼 수도 있을 것 같습니다.
                            What if we tried this instead? — 대신 이렇게 해보는 건 어떨까요?
                            I'd like to suggest an alternative. — 대안을 하나 제안하고 싶습니다."""),
                    new LessonSeed("정중하지만 단호하게 반대하기", LessonType.VOCAB, """
                            I understand your point, but I have to disagree. — 말씀하신 요점은 이해하지만, 동의하기는 어렵습니다.
                            I'm afraid that won't work for us. — 죄송하지만 저희 쪽에서는 그렇게 진행하기 어렵습니다.
                            We need to hold firm on this point. — 이 부분에 대해서는 입장을 유지해야 할 것 같습니다."""),
                    new LessonSeed("명확한 설명 요청하기", LessonType.VOCAB, """
                            Could you clarify what you mean by that? — 그게 무슨 의미인지 좀 더 설명해 주시겠어요?
                            I'm not sure I fully understand your position. — 말씀하신 입장을 완전히 이해했는지 잘 모르겠습니다.
                            Can you walk me through the details again? — 세부 사항을 다시 한번 설명해 주시겠어요?"""),
                    new LessonSeed("우려 표현하기", LessonType.VOCAB, """
                            I have some concerns about this proposal. — 이 제안에 대해 몇 가지 우려되는 부분이 있습니다.
                            I'm worried this might cause delays down the line. — 이 부분이 나중에 지연을 일으킬까 걱정됩니다.
                            Could we address this issue before moving forward? — 진행하기 전에 이 문제를 먼저 논의할 수 있을까요?"""),
                    new LessonSeed("최종 합의 이끌어내기", LessonType.VOCAB, """
                            I think we've reached a common ground. — 우리가 공통된 합의점에 도달한 것 같습니다.
                            Let's finalize the terms and move forward. — 조건을 확정하고 진행하도록 하죠.
                            I'm glad we could come to an agreement. — 합의에 이를 수 있어서 기쁩니다.""")
            )),
            new CourseSeed("프레젠테이션 영어 표현", "발표 시작부터 질의응답까지 쓰는 표현을 배우는 코스입니다.", "📊",
                    MemberType.ADULT, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("발표 시작하기", LessonType.VOCAB, """
                            Today, I'll talk about... — 오늘은 ...에 대해 말씀드리겠습니다.
                            Let me start with an overview. — 개요부터 말씀드리겠습니다."""),
                    new LessonSeed("질문 받기", LessonType.VOCAB, """
                            Any questions so far? — 지금까지 질문 있으신가요?
                            That's a great question. — 좋은 질문이네요.
                            Let me get back to you on that. — 그 부분은 다시 확인해서 알려드릴게요."""),
                    new LessonSeed("슬라이드 전환하기", LessonType.VOCAB, """
                            Now, let's move on to the next section. — 이제 다음 섹션으로 넘어가겠습니다.
                            Turning to the second point, — 두 번째 포인트로 넘어가서,
                            This brings us to our next topic. — 이제 다음 주제로 이어집니다."""),
                    new LessonSeed("핵심 데이터 강조하기", LessonType.VOCAB, """
                            As you can see from this chart, sales have increased significantly. — 이 차트에서 보시다시피 매출이 크게 증가했습니다.
                            I'd like to draw your attention to this figure. — 이 수치에 주목해 주시기 바랍니다.
                            This number is particularly important. — 이 수치가 특히 중요합니다."""),
                    new LessonSeed("발표 요약 및 마무리하기", LessonType.VOCAB, """
                            To sum up, — 요약하자면,
                            Let me recap the main points. — 주요 내용을 다시 한번 정리해 드리겠습니다.
                            In conclusion, we believe this strategy will drive growth. — 결론적으로, 이 전략이 성장을 이끌 것이라 생각합니다."""),
                    new LessonSeed("까다로운 질문에 대응하기", LessonType.VOCAB, """
                            That's a challenging question, let me think about it for a moment. — 어려운 질문이네요, 잠시 생각해 보겠습니다.
                            I don't have that data on hand right now, but I'll follow up. — 지금 당장 그 자료는 없지만, 추후에 답변드리겠습니다.
                            Could you clarify what you're specifically asking about? — 구체적으로 어떤 부분을 질문하시는지 설명해 주시겠어요?"""),
                    new LessonSeed("청중에게 감사 인사하기", LessonType.VOCAB, """
                            Thank you all for your time and attention. — 시간을 내어 경청해 주셔서 감사합니다.
                            I appreciate your valuable feedback. — 소중한 피드백에 감사드립니다.
                            Thank you, and I'll be happy to answer more questions afterward. — 감사합니다, 이후에도 질문에 기꺼이 답변드리겠습니다.""")
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
                            On the other hand, — 반면에,"""),
                    new LessonSeed("근거와 통계 인용하기", LessonType.VOCAB, """
                            According to recent statistics, — 최근 통계에 따르면,
                            Studies have shown that this policy reduces unemployment. — 연구에 따르면 이 정책이 실업률을 줄이는 것으로 나타났습니다.
                            The data clearly supports this argument. — 이 데이터는 이 주장을 명확히 뒷받침합니다."""),
                    new LessonSeed("일부 인정하며 입장 유지하기", LessonType.VOCAB, """
                            I concede that there are some valid concerns, but my position remains unchanged. — 몇 가지 타당한 우려가 있다는 점은 인정하지만, 제 입장은 변함없습니다.
                            While that may be true in some cases, the overall trend suggests otherwise. — 일부 경우에는 그럴 수 있지만, 전반적인 추세는 다르게 나타납니다.
                            Even so, I still believe this is the right approach. — 그럼에도 불구하고, 저는 여전히 이것이 옳은 접근이라고 생각합니다."""),
                    new LessonSeed("해결책 제안하기", LessonType.VOCAB, """
                            One possible solution would be to implement stricter regulations. — 가능한 해결책 중 하나는 더 엄격한 규제를 시행하는 것입니다.
                            I propose that we take a more gradual approach. — 저는 좀 더 점진적인 접근을 취할 것을 제안합니다.
                            This solution could address the root cause of the problem. — 이 해결책은 문제의 근본 원인을 해결할 수 있을 것입니다."""),
                    new LessonSeed("전제에 의문 제기하기", LessonType.VOCAB, """
                            That assumption doesn't necessarily hold true. — 그 전제가 반드시 사실인 것은 아닙니다.
                            Is it fair to assume that everyone benefits equally? — 모두가 동등하게 혜택을 받는다고 가정하는 것이 타당할까요?
                            We should question whether this premise is accurate. — 이 전제가 정확한지 의문을 가져야 합니다."""),
                    new LessonSeed("토론 내용 정리하기", LessonType.VOCAB, """
                            To summarize both sides of the debate, — 토론의 양측 입장을 요약하자면,
                            The key disagreement lies in how we interpret the data. — 핵심적인 이견은 데이터를 해석하는 방식에 있습니다.
                            Ultimately, further discussion is needed to reach a conclusion. — 결국, 결론에 이르기 위해서는 추가적인 논의가 필요합니다.""")
            )),
            new CourseSeed("원서로 읽는 짧은 에세이", "짧은 영어 에세이를 읽고 핵심을 파악하는 심화 읽기 코스입니다.", "📜",
                    MemberType.ADULT, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("주제문 찾기", LessonType.READING, """
                            The main idea of this essay is... — 이 글의 핵심은...
                            The author begins by describing... — 글쓴이는 ...을 설명하며 시작한다."""),
                    new LessonSeed("글쓴이 의도 파악하기", LessonType.READING, """
                            The author suggests that... — 글쓴이는 ...라고 제안한다.
                            This implies that... — 이것은 ...을 암시한다."""),
                    new LessonSeed("글의 어조 파악하기", LessonType.READING, """
                            The tone of this passage is rather critical. — 이 글의 어조는 다소 비판적입니다.
                            The author's tone shifts from optimistic to cautious. — 글쓴이의 어조는 낙관적에서 신중한 태도로 바뀝니다.
                            We can infer the tone from the word choice used here. — 여기서 사용된 단어 선택을 통해 어조를 유추할 수 있습니다."""),
                    new LessonSeed("뒷받침하는 근거 찾기", LessonType.READING, """
                            The author supports this claim with several examples. — 글쓴이는 여러 예시로 이 주장을 뒷받침한다.
                            This evidence directly supports the main argument. — 이 근거는 주요 주장을 직접적으로 뒷받침한다.
                            Notice how the second paragraph provides further evidence. — 두 번째 문단이 추가 근거를 제시하는 방식에 주목하라."""),
                    new LessonSeed("두 관점 비교하기", LessonType.READING, """
                            The first viewpoint emphasizes individual responsibility, while the second focuses on social structures. — 첫 번째 관점은 개인의 책임을 강조하는 반면, 두 번째 관점은 사회 구조에 초점을 맞춘다.
                            These two perspectives contradict each other on this point. — 이 두 관점은 이 지점에서 서로 상충한다.
                            Comparing both views helps us understand the complexity of the issue. — 두 관점을 비교하면 이 사안의 복잡성을 이해하는 데 도움이 된다."""),
                    new LessonSeed("글쓴이의 결론 평가하기", LessonType.READING, """
                            The author's conclusion seems well-supported by the evidence presented. — 글쓴이의 결론은 제시된 근거로 잘 뒷받침되는 것으로 보인다.
                            However, the conclusion overlooks an important counterargument. — 그러나 이 결론은 중요한 반론을 간과하고 있다.
                            Do you find the author's final judgment convincing? — 글쓴이의 최종 판단이 설득력 있다고 생각하는가?"""),
                    new LessonSeed("지문 요약하기", LessonType.READING, """
                            In summary, this passage discusses the impact of technology on daily life. — 요약하자면, 이 글은 기술이 일상생활에 미치는 영향을 다룬다.
                            The passage can be summarized in one sentence. — 이 지문은 한 문장으로 요약될 수 있다.
                            Let's summarize the key points before moving on. — 다음으로 넘어가기 전에 핵심 내용을 요약해 보자.""")
            )),
            new CourseSeed("인터뷰 영어 표현 정리", "면접과 인터뷰에서 쓰는 표현을 정리하는 실전 코스입니다.", "🎤",
                    MemberType.ADULT, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("자기소개하기", LessonType.VOCAB, """
                            Let me introduce myself. — 제 소개를 하겠습니다.
                            I have three years of experience in this field. — 이 분야에서 3년의 경력이 있습니다."""),
                    new LessonSeed("강점 말하기", LessonType.VOCAB, """
                            My strength is... — 저의 강점은...
                            I'm confident in my ability to... — 저는 ...하는 능력에 자신이 있습니다."""),
                    new LessonSeed("지원 동기 답하기", LessonType.VOCAB, """
                            I'm particularly drawn to this role because it aligns with my long-term goals. — 이 직무가 저의 장기적인 목표와 부합하기 때문에 특히 끌립니다.
                            I've always admired this company's commitment to innovation. — 저는 항상 이 회사의 혁신에 대한 헌신을 존경해 왔습니다.
                            This position offers exactly the kind of challenge I'm looking for. — 이 직무는 제가 찾던 바로 그런 도전을 제공합니다."""),
                    new LessonSeed("극복한 어려움 설명하기", LessonType.VOCAB, """
                            I once faced a tight deadline with limited resources. — 저는 한때 제한된 자원으로 촉박한 마감을 겪은 적이 있습니다.
                            I overcame this challenge by prioritizing tasks and communicating clearly with my team. — 저는 업무의 우선순위를 정하고 팀과 명확히 소통함으로써 이 어려움을 극복했습니다.
                            That experience taught me how to stay calm under pressure. — 그 경험을 통해 압박 속에서도 침착함을 유지하는 법을 배웠습니다."""),
                    new LessonSeed("면접관에게 질문하기", LessonType.VOCAB, """
                            Could you tell me more about the team I'd be working with? — 제가 함께 일하게 될 팀에 대해 좀 더 말씀해 주시겠어요?
                            What does success look like in this role? — 이 직무에서 성공이란 어떤 모습인가요?
                            Is there anything else you'd like to know about my background? — 제 경력에 대해 더 궁금하신 점이 있으신가요?"""),
                    new LessonSeed("연봉과 기대 사항 논의하기", LessonType.VOCAB, """
                            I'd like to discuss the salary range for this position. — 이 직무의 연봉 범위에 대해 논의하고 싶습니다.
                            My expectations are in line with the industry standard. — 저의 기대치는 업계 표준에 부합합니다.
                            I'm open to discussing the details further. — 세부 사항은 추가로 논의할 의향이 있습니다."""),
                    new LessonSeed("면접 잘 마무리하기", LessonType.VOCAB, """
                            Thank you for taking the time to meet with me today. — 오늘 시간을 내어 만나 주셔서 감사합니다.
                            I'm very enthusiastic about the possibility of joining your team. — 귀하의 팀에 합류할 가능성에 대해 매우 기대하고 있습니다.
                            I look forward to hearing from you soon. — 곧 좋은 소식 들을 수 있기를 기대하겠습니다.""")
            ))
    );
}
