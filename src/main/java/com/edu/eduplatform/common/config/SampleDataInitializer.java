package com.edu.eduplatform.common.config;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
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
                        .build());
            }
        }
    }

    private record LessonSeed(String title, String content) {
    }

    private record CourseSeed(
            String title,
            String description,
            MemberType targetType,
            EnglishLevel level,
            List<LessonSeed> lessons
    ) {
    }

    private static final List<CourseSeed> COURSE_SEEDS = List.of(

            // ---------- CHILD / BEGINNER ----------
            new CourseSeed("파닉스로 시작하는 알파벳", "그림과 소리로 배우는 첫 알파벳, 짧은 레슨 위주로 구성했습니다.",
                    MemberType.CHILD, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("알파벳 A부터 E까지", "A는 Apple(사과)의 A예요.\nB는 Banana(바나나)의 B예요."),
                    new LessonSeed("알파벳 F부터 J까지", "F는 Fish(물고기)의 F예요.\nG는 Grape(포도)의 G예요."),
                    new LessonSeed("알파벳 K부터 O까지", "K는 King(왕)의 K예요.\nL은 Lion(사자)의 L이에요.")
            )),
            new CourseSeed("그림으로 배우는 첫 영단어", "그림 카드로 색깔과 사물의 이름을 배우는 어휘 중심 코스입니다.",
                    MemberType.CHILD, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("색깔 이름 배우기", "Red apple. — 빨간 사과.\nBlue sky. — 파란 하늘."),
                    new LessonSeed("동물 이름 배우기", "Cat. — 고양이.\nDog. — 강아지.")
            )),
            new CourseSeed("소리 내어 읽는 첫 문장", "짧은 문장을 소리 내어 읽으며 읽기 자신감을 키우는 코스입니다.",
                    MemberType.CHILD, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("짧은 인사 문장", "Hi! — 안녕!\nBye! — 잘 가!"),
                    new LessonSeed("짧은 소개 문장", "I am Tom. — 나는 Tom이에요.\nI am seven. — 나는 일곱 살이에요.")
            )),

            // ---------- CHILD / ELEMENTARY ----------
            new CourseSeed("짧은 이야기로 배우는 영어", "짧은 이야기를 읽고 줄거리를 이해하는 읽기 코스입니다.",
                    MemberType.CHILD, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("이야기 속 인물 찾기", "This is Sam. — 이 아이는 Sam이에요.\nSam has a dog. — Sam은 강아지가 있어요."),
                    new LessonSeed("이야기 순서 이해하기", "First, Sam wakes up. — 먼저 Sam이 일어나요.\nThen, Sam eats breakfast. — 그다음 아침을 먹어요.")
            )),
            new CourseSeed("동물 이름과 색깔 표현", "동물과 색깔을 조합한 표현을 배우는 어휘 확장 코스입니다.",
                    MemberType.CHILD, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("동물 색깔 말하기", "A brown dog. — 갈색 강아지.\nA white cat. — 하얀 고양이."),
                    new LessonSeed("좋아하는 동물 말하기", "I like rabbits. — 나는 토끼를 좋아해요.\nDo you like cats? — 고양이 좋아해요?")
            )),
            new CourseSeed("학교에서 쓰는 영어 표현", "교실에서 자주 쓰는 표현을 배우는 생활 회화 코스입니다.",
                    MemberType.CHILD, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("교실 물건 이름", "This is my pencil. — 이건 내 연필이에요.\nWhere is my book? — 내 책이 어디 있지?"),
                    new LessonSeed("선생님께 말하기", "May I go to the bathroom? — 화장실 가도 될까요?\nI don't understand. — 이해가 안 돼요.")
            )),

            // ---------- CHILD / INTERMEDIATE ----------
            new CourseSeed("짧은 대화문 읽고 이해하기", "두 사람의 짧은 대화를 읽고 내용을 파악하는 코스입니다.",
                    MemberType.CHILD, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("친구와의 대화", "A: What's your favorite color? — 제일 좋아하는 색이 뭐야?\nB: I like blue. — 난 파란색이 좋아."),
                    new LessonSeed("가족과의 대화", "A: Where is Dad? — 아빠 어디 계셔?\nB: He is at work. — 회사에 계셔.")
            )),
            new CourseSeed("일기 쓰기로 배우는 문장 구조", "하루 일과를 문장으로 써보며 문장 구조를 익히는 쓰기 코스입니다.",
                    MemberType.CHILD, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("오늘 한 일 쓰기", "Today, I played soccer. — 오늘 나는 축구를 했어요.\nIt was fun. — 재미있었어요."),
                    new LessonSeed("내일 할 일 쓰기", "Tomorrow, I will visit my grandma. — 내일 할머니 댁에 갈 거예요.")
            )),
            new CourseSeed("좋아하는 것 소개하기", "취미와 좋아하는 것을 소개하는 말하기·쓰기 코스입니다.",
                    MemberType.CHILD, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("취미 소개하기", "My hobby is drawing. — 제 취미는 그림 그리기예요."),
                    new LessonSeed("좋아하는 음식 소개하기", "My favorite food is pizza. — 제가 좋아하는 음식은 피자예요.")
            )),

            // ---------- CHILD / ADVANCED ----------
            new CourseSeed("짧은 영어 동화 읽기", "쉬운 영어 동화를 읽고 교훈을 이해하는 심화 읽기 코스입니다.",
                    MemberType.CHILD, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("동화 속 교훈 찾기", "The rabbit was too proud. — 토끼는 너무 자만했어요.\nSlow and steady wins the race. — 느려도 꾸준한 게 이겨요."),
                    new LessonSeed("동화 다시 말하기", "Once upon a time... — 옛날 옛적에...\nAnd they lived happily ever after. — 그리고 그들은 행복하게 살았답니다.")
            )),
            new CourseSeed("그림책 속 표현 정리하기", "그림책에 나온 표현을 정리하고 활용해보는 코스입니다.",
                    MemberType.CHILD, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("그림책 표현 모으기", "It was a dark and stormy night. — 어둡고 폭풍우 치는 밤이었어요."),
                    new LessonSeed("표현 바꿔 말하기", "It was a bright and sunny day. — 밝고 화창한 날이었어요.")
            )),
            new CourseSeed("나만의 이야기 만들기", "배운 표현을 활용해 짧은 이야기를 직접 만들어보는 코스입니다.",
                    MemberType.CHILD, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("이야기 시작 문장 쓰기", "One day, a boy found a map. — 어느 날, 한 소년이 지도를 발견했어요."),
                    new LessonSeed("이야기 마무리하기", "In the end, everyone was happy. — 결국 모두가 행복했어요.")
            )),

            // ---------- ADULT / BEGINNER ----------
            new CourseSeed("왕초보 성인 영어 회화", "인사, 자기소개부터 실생활 표현까지 — 말하기 중심의 입문 코스입니다.",
                    MemberType.ADULT, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("인사와 자기소개", "Hello! My name is Jane. — 안녕하세요! 제 이름은 Jane입니다.\nNice to meet you. — 만나서 반갑습니다."),
                    new LessonSeed("숫자와 시간 표현", "What time is it? — 지금 몇 시예요?\nIt's three thirty. — 3시 30분이에요."),
                    new LessonSeed("쇼핑할 때 쓰는 표현", "How much is this? — 이거 얼마예요?\nI'll take this one. — 이걸로 할게요."),
                    new LessonSeed("식당에서 주문하기", "Can I get the menu, please? — 메뉴 좀 주시겠어요?\nI'd like a coffee. — 커피 한 잔 주세요.")
            )),
            new CourseSeed("여행 영어 실전 표현", "공항, 숙소, 식당에서 바로 쓰는 여행 필수 표현 코스입니다.",
                    MemberType.ADULT, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("공항에서 쓰는 표현", "Where is the gate? — 게이트가 어디예요?\nI'd like a window seat. — 창가 자리로 주세요."),
                    new LessonSeed("숙소에서 쓰는 표현", "I have a reservation. — 예약했어요.\nWhat time is check-out? — 체크아웃이 몇 시예요?")
            )),
            new CourseSeed("하루 일과 말하기", "일상적인 하루 일과를 영어로 말해보는 입문 코스입니다.",
                    MemberType.ADULT, EnglishLevel.BEGINNER, List.of(
                    new LessonSeed("아침 일과 말하기", "I wake up at seven. — 저는 7시에 일어나요.\nI have breakfast. — 아침을 먹어요."),
                    new LessonSeed("저녁 일과 말하기", "I go home at six. — 저는 6시에 집에 가요.\nI watch TV. — TV를 봐요.")
            )),

            // ---------- ADULT / ELEMENTARY ----------
            new CourseSeed("이메일과 메시지 영어 표현", "업무·일상 이메일과 메시지에서 자주 쓰는 표현을 배우는 코스입니다.",
                    MemberType.ADULT, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("이메일 시작·끝맺음", "Dear Mr. Kim, — 김 선생님께,\nBest regards, — 감사합니다,"),
                    new LessonSeed("메시지로 약속 잡기", "Are you free tomorrow? — 내일 시간 있어요?\nLet's meet at 3. — 3시에 만나요.")
            )),
            new CourseSeed("회의에서 자주 쓰는 표현", "회의 시작부터 마무리까지 쓰이는 표현을 배우는 코스입니다.",
                    MemberType.ADULT, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("회의 시작하기", "Let's get started. — 시작하겠습니다.\nShall we begin? — 시작할까요?"),
                    new LessonSeed("의견 묻기", "What do you think? — 어떻게 생각하세요?\nAny other opinions? — 다른 의견 있으세요?")
            )),
            new CourseSeed("전화 영어 기초", "전화 통화에서 쓰는 기본 표현을 배우는 코스입니다.",
                    MemberType.ADULT, EnglishLevel.ELEMENTARY, List.of(
                    new LessonSeed("전화 받고 걸기", "May I speak to Mr. Lee? — 이 선생님과 통화할 수 있을까요?\nHold on, please. — 잠시만요."),
                    new LessonSeed("전화로 약속 정하기", "Can we reschedule? — 일정을 조정할 수 있을까요?\nI'll call you back. — 다시 전화드릴게요.")
            )),

            // ---------- ADULT / INTERMEDIATE ----------
            new CourseSeed("비즈니스 영어 이메일 작성", "업무 상황별 이메일을 작성해보는 실용 쓰기 코스입니다.",
                    MemberType.ADULT, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("요청 이메일 쓰기", "Could you send me the file? — 파일을 보내주실 수 있나요?\nThank you in advance. — 미리 감사드립니다."),
                    new LessonSeed("사과 이메일 쓰기", "I apologize for the delay. — 지연에 대해 사과드립니다.")
            )),
            new CourseSeed("협상과 의견 표현하기", "의견을 정중하게 주고받는 표현을 배우는 코스입니다.",
                    MemberType.ADULT, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("동의·반대 표현", "I agree with that. — 그 의견에 동의해요.\nI see it differently. — 저는 다르게 생각해요."),
                    new LessonSeed("타협안 제시하기", "How about a compromise? — 절충안은 어떨까요?")
            )),
            new CourseSeed("프레젠테이션 영어 표현", "발표 시작부터 질의응답까지 쓰는 표현을 배우는 코스입니다.",
                    MemberType.ADULT, EnglishLevel.INTERMEDIATE, List.of(
                    new LessonSeed("발표 시작하기", "Today, I'll talk about... — 오늘은 ...에 대해 말씀드리겠습니다."),
                    new LessonSeed("질문 받기", "Any questions so far? — 지금까지 질문 있으신가요?")
            )),

            // ---------- ADULT / ADVANCED ----------
            new CourseSeed("시사 이슈로 배우는 영어 토론", "시사 주제로 의견을 주고받는 토론 표현을 배우는 코스입니다.",
                    MemberType.ADULT, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("주장 펼치기", "In my opinion, — 제 생각에는,\nThe evidence suggests that... — 증거에 따르면..."),
                    new LessonSeed("반박하기", "That's a fair point, but... — 타당한 지적이지만...\nI'd like to add that... — 덧붙이자면...")
            )),
            new CourseSeed("원서로 읽는 짧은 에세이", "짧은 영어 에세이를 읽고 핵심을 파악하는 심화 읽기 코스입니다.",
                    MemberType.ADULT, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("주제문 찾기", "The main idea of this essay is... — 이 글의 핵심은..."),
                    new LessonSeed("글쓴이 의도 파악하기", "The author suggests that... — 글쓴이는 ...라고 제안한다.")
            )),
            new CourseSeed("인터뷰 영어 표현 정리", "면접과 인터뷰에서 쓰는 표현을 정리하는 실전 코스입니다.",
                    MemberType.ADULT, EnglishLevel.ADVANCED, List.of(
                    new LessonSeed("자기소개하기", "Let me introduce myself. — 제 소개를 하겠습니다."),
                    new LessonSeed("강점 말하기", "My strength is... — 저의 강점은...")
            ))
    );
}
