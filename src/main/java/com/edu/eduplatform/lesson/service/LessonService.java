package com.edu.eduplatform.lesson.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse.ContentLine;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse.LessonQuiz;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse.LineType;
import com.edu.eduplatform.lesson.dto.LessonSummaryResponse;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.MemberType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final IconCatalog iconCatalog;

    public List<LessonSummaryResponse> listByCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderNoAsc(courseId).stream()
                .map(lesson -> new LessonSummaryResponse(
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getOrderNo(),
                        lesson.getLessonType(),
                        imageFor(firstContentIcon(lesson.getContent()))
                ))
                .toList();
    }

    public LessonDetailResponse getDetail(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new LessonNotFoundException(id));
        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException(lesson.getCourseId()));
        List<Lesson> siblings = lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId());

        int index = IntStream.range(0, siblings.size())
                .filter(i -> siblings.get(i).getId().equals(lesson.getId()))
                .findFirst()
                .orElseThrow();
        Lesson prev = index > 0 ? siblings.get(index - 1) : null;
        Lesson next = index < siblings.size() - 1 ? siblings.get(index + 1) : null;
        List<ContentLine> contentLines = parseContent(lesson.getContent());

        return new LessonDetailResponse(
                lesson.getId(),
                course.getId(),
                course.getTitle(),
                lesson.getTitle(),
                lesson.getLessonType(),
                lesson.getOrderNo(),
                contentLines,
                siblings.size(),
                prev != null ? prev.getId() : null,
                prev != null ? prev.getTitle() : null,
                next != null ? next.getId() : null,
                next != null ? next.getTitle() : null,
                buildQuiz(lesson, contentLines, siblings)
        );
    }

    /**
     * 레슨을 완료 처리하기 전 정답을 확인할 때 쓰는, 퀴즈 정답 단어만 도출하는 경량 버전.
     * {@link #buildQuiz}와 같은 문장·단어 선택 로직({@link #chooseQuizWord})을 공유해 항상 같은 정답을 낸다.
     */
    public Optional<String> deriveQuizAnswer(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));
        return chooseQuizWord(lesson, parseContent(lesson.getContent())).map(ChosenQuizWord::word);
    }

    /**
     * 레슨의 PHRASE 문장 하나에서 핵심 단어를 빈칸으로 만들어 이해도 확인 퀴즈를 즉석에서 만든다.
     * 새 콘텐츠를 저장하지 않는다(stateless) — 정답도 매번 같은 알고리즘으로 다시 계산해서 검증한다.
     * 오답 보기는 같은 코스의 다른 레슨들의 PHRASE 문장에서 모은다. PHRASE 문장이 없거나 오답 후보가
     * 하나도 없으면 퀴즈 없이 null을 반환한다 — 기존 "레슨 없으면 정직하게 빈 상태" 컨벤션과 같다.
     */
    private LessonQuiz buildQuiz(Lesson lesson, List<ContentLine> contentLines, List<Lesson> siblings) {
        Optional<ChosenQuizWord> picked = chooseQuizWord(lesson, contentLines);
        if (picked.isEmpty()) {
            return null;
        }
        ContentLine sentence = picked.get().sentence();
        String answer = picked.get().word();

        Set<String> distractorPool = new LinkedHashSet<>();
        for (Lesson sibling : siblings) {
            if (sibling.getId().equals(lesson.getId())) {
                continue;
            }
            for (ContentLine line : parseContent(sibling.getContent())) {
                if (line.type() != LineType.PHRASE) {
                    continue;
                }
                QuizWordPicker.extractKeyWord(line.text())
                        .filter(word -> !word.equalsIgnoreCase(answer))
                        .ifPresent(distractorPool::add);
            }
        }
        if (distractorPool.isEmpty()) {
            return null;
        }

        List<String> options = new ArrayList<>();
        options.add(answer);
        options.addAll(QuizWordPicker.pickDistractors(distractorPool, answer, 3, new Random()));
        Collections.shuffle(options);

        return new LessonQuiz(QuizWordPicker.blankOut(sentence.text(), answer), sentence.subtext(), options);
    }

    /**
     * 레슨 id로 결정론적으로 PHRASE 문장 하나를 고르고(같은 레슨이면 항상 같은 문장), 그 문장에서 핵심 단어를
     * 뽑는다. PHRASE 문장이 없거나 핵심 단어를 못 찾으면(불용어뿐인 문장 등) 빈 값을 반환한다.
     */
    private Optional<ChosenQuizWord> chooseQuizWord(Lesson lesson, List<ContentLine> contentLines) {
        List<ContentLine> phraseLines = contentLines.stream()
                .filter(line -> line.type() == LineType.PHRASE)
                .toList();
        if (phraseLines.isEmpty()) {
            return Optional.empty();
        }
        ContentLine chosen = phraseLines.get(Math.floorMod(lesson.getId(), phraseLines.size()));
        return QuizWordPicker.extractKeyWord(chosen.text()).map(word -> new ChosenQuizWord(chosen, word));
    }

    private record ChosenQuizWord(ContentLine sentence, String word) {
    }

    /**
     * 대상(CHILD/ADULT)의 공식 코스 전체에서 아이콘이 붙은 PHRASE 문장의 핵심 단어·아이콘 쌍을 모은다.
     * 그림 퀴즈(픽처 퀴즈)의 재료 풀로 쓰인다 — 새 콘텐츠 없이 기존 레슨 카드 아이콘을 재사용.
     * 같은 단어가 여러 번 나오면 처음 나온 것만 남긴다(대소문자 무시 중복 제거).
     */
    public List<IconPair> collectIconPairs(MemberType targetType) {
        Map<String, IconPair> pairsByWord = new LinkedHashMap<>();
        for (Course course : courseRepository.search(targetType, null, null)) {
            for (Lesson lesson : lessonRepository.findByCourseIdOrderByOrderNoAsc(course.getId())) {
                for (ContentLine line : parseContent(lesson.getContent())) {
                    if (line.type() != LineType.PHRASE || line.iconImage() == null) {
                        continue;
                    }
                    QuizWordPicker.extractKeyWord(line.text())
                            .ifPresent(word -> pairsByWord.putIfAbsent(
                                    word.toLowerCase(), new IconPair(word, line.iconImage())));
                }
            }
        }
        return List.copyOf(pairsByWord.values());
    }

    public record IconPair(String word, String iconImage) {
    }

    /** 레슨 콘텐츠 문자열을 INTRO/PHRASE/NOTE 줄로 파싱한다 — 다른 서비스(그림/매일 단어 퀴즈)도 재사용. */
    public List<ContentLine> parseContent(String content) {
        return content.lines()
                .map(String::strip)
                .filter(line -> !line.isBlank())
                .map(this::toContentLine)
                .toList();
    }

    private ContentLine toContentLine(String line) {
        if (line.startsWith("INTRO:")) {
            IconSplit split = splitLeadingIcon(line.substring("INTRO:".length()).strip());
            return new ContentLine(LineType.INTRO, split.text(), null, split.icon(), imageFor(split.icon()));
        }

        int dashIndex = line.indexOf(" — ");
        if (dashIndex >= 0) {
            String english = line.substring(0, dashIndex).strip();
            String korean = line.substring(dashIndex + " — ".length()).strip();
            IconSplit split = splitLeadingIcon(english);
            return new ContentLine(LineType.PHRASE, split.text(), korean, split.icon(), imageFor(split.icon()));
        }

        IconSplit split = splitLeadingIcon(line);
        return new ContentLine(LineType.NOTE, split.text(), null, split.icon(), imageFor(split.icon()));
    }

    private String imageFor(String icon) {
        return iconCatalog.resolveImagePath(icon);
    }

    /**
     * 레슨 목록에서 썸네일로 쓸 대표 이모지 하나를 고른다.
     * INTRO(마스코트 인사) 줄은 모든 레슨에서 반복돼 대표성이 없으므로 건너뛰고,
     * 실제 학습 내용이 담긴 첫 줄의 이모지를 사용한다.
     */
    private String firstContentIcon(String content) {
        return content.lines()
                .map(String::strip)
                .filter(line -> !line.isBlank() && !line.startsWith("INTRO:"))
                .findFirst()
                .map(this::splitLeadingIcon)
                .map(IconSplit::icon)
                .orElse(null);
    }

    /**
     * 콘텐츠 줄 맨 앞의 이모지(예: "🍎 Red apple.")를 본문과 분리해 카드에서 큰 아이콘으로 보여줄 수 있게 한다.
     * 이모지 토큰은 알파벳을 포함하지 않는다는 점으로 구분한다 — 영어 문장은 항상 알파벳으로 시작하므로 오탐이 없다.
     */
    private IconSplit splitLeadingIcon(String line) {
        int spaceIndex = line.indexOf(' ');
        if (spaceIndex <= 0) {
            return new IconSplit(null, line);
        }
        String firstToken = line.substring(0, spaceIndex);
        if (firstToken.chars().anyMatch(Character::isLetter)) {
            return new IconSplit(null, line);
        }
        return new IconSplit(firstToken, line.substring(spaceIndex + 1).strip());
    }

    private record IconSplit(String icon, String text) {
    }
}
