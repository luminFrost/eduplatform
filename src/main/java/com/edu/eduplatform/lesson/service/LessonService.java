package com.edu.eduplatform.lesson.service;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.exception.CourseNotFoundException;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse.ContentLine;
import com.edu.eduplatform.lesson.dto.LessonDetailResponse.LineType;
import com.edu.eduplatform.lesson.dto.LessonSummaryResponse;
import com.edu.eduplatform.lesson.exception.LessonNotFoundException;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import java.util.List;
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

        return new LessonDetailResponse(
                lesson.getId(),
                course.getId(),
                course.getTitle(),
                lesson.getTitle(),
                lesson.getOrderNo(),
                parseContent(lesson.getContent()),
                siblings.size(),
                prev != null ? prev.getId() : null,
                prev != null ? prev.getTitle() : null,
                next != null ? next.getId() : null,
                next != null ? next.getTitle() : null
        );
    }

    private List<ContentLine> parseContent(String content) {
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
