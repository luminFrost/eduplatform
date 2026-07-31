package com.edu.eduplatform.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.lesson.domain.Lesson;
import com.edu.eduplatform.lesson.domain.LessonType;
import com.edu.eduplatform.lesson.repository.LessonRepository;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Test
    void search_필터_조합에_따라_코스를_반환한다() {
        courseRepository.save(Course.builder()
                .title("파닉스 알파벳").description("설명")
                .targetType(MemberType.CHILD).level(EnglishLevel.BEGINNER).build());
        courseRepository.save(Course.builder()
                .title("왕초보 회화").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        courseRepository.save(Course.builder()
                .title("비즈니스 영어").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.INTERMEDIATE).build());

        assertThat(courseRepository.search(null, null, null)).hasSize(3);
        assertThat(courseRepository.search(MemberType.ADULT, null, null)).hasSize(2);
        assertThat(courseRepository.search(null, EnglishLevel.BEGINNER, null)).hasSize(2);
        assertThat(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null))
                .extracting(Course::getTitle)
                .containsExactly("왕초보 회화");
    }

    @Test
    void search_개인_코스는_결과에서_제외한다() {
        courseRepository.save(Course.builder()
                .title("공식 코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        courseRepository.save(Course.builder()
                .title("개인 코스").description("설명").ownerId(1L)
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        assertThat(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER, null))
                .extracting(Course::getTitle)
                .containsExactly("공식 코스");
    }

    @Test
    void search_영역_필터로_해당_레슨타입을_가진_코스만_반환한다() {
        Course vocabCourse = courseRepository.save(Course.builder()
                .title("어휘코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Course readingCourse = courseRepository.save(Course.builder()
                .title("읽기코스").description("설명")
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        lessonRepository.save(Lesson.builder()
                .courseId(vocabCourse.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.VOCAB).build());
        lessonRepository.save(Lesson.builder()
                .courseId(readingCourse.getId()).orderNo(1).title("1과")
                .content("내용").lessonType(LessonType.READING).build());

        assertThat(courseRepository.search(null, null, LessonType.VOCAB))
                .extracting(Course::getTitle)
                .containsExactly("어휘코스");
    }

    @Test
    void findByOwnerIdOrderByIdDesc_해당_회원의_개인_코스만_최신순으로_반환한다() {
        courseRepository.save(Course.builder()
                .title("남의 개인 코스").description("설명").ownerId(2L)
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Course first = courseRepository.save(Course.builder()
                .title("내 첫 코스").description("설명").ownerId(1L)
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());
        Course second = courseRepository.save(Course.builder()
                .title("내 두번째 코스").description("설명").ownerId(1L)
                .targetType(MemberType.ADULT).level(EnglishLevel.BEGINNER).build());

        assertThat(courseRepository.findByOwnerIdOrderByIdDesc(1L))
                .extracting(Course::getId)
                .containsExactly(second.getId(), first.getId());
    }
}
