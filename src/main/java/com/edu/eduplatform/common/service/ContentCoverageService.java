package com.edu.eduplatform.common.service;

import com.edu.eduplatform.common.dto.CoverageReportRow;
import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.course.repository.CourseRepository;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.domain.Question;
import com.edu.eduplatform.question.repository.QuestionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 8개 대상·레벨 조합이 각 영역(어휘/읽기/쓰기/듣기/말하기)에 레슨·문항을 얼마나 갖췄는지 집계한다.
 * Course/Lesson/Question 세 도메인을 동등하게 넘나들어 특정 도메인 서비스에 얹지 않고 여기 둔다
 * (HomeController가 "특정 도메인에 안 속하는 화면"의 자리로 쓰이는 것과 같은 이유).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentCoverageService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;

    public List<CoverageReportRow> getCoverageReport() {
        List<CoverageReportRow> rows = new ArrayList<>();
        for (MemberType target : MemberType.values()) {
            for (EnglishLevel level : EnglishLevel.values()) {
                List<Course> courses = courseRepository.search(target, level, null, null);
                Map<LessonType, Integer> lessonCounts = lessonRepository
                        .findByCourseIdIn(courses.stream().map(Course::getId).toList()).stream()
                        .collect(Collectors.groupingBy(Lesson::getLessonType, Collectors.summingInt(l -> 1)));
                Map<LessonType, Integer> questionCounts = questionRepository.findByTargetTypeAndLevel(target, level).stream()
                        .collect(Collectors.groupingBy(Question::getLessonType, Collectors.summingInt(q -> 1)));
                rows.add(new CoverageReportRow(target, level, lessonCounts, questionCounts));
            }
        }
        return rows;
    }
}
