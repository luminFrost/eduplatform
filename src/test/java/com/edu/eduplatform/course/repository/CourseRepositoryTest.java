package com.edu.eduplatform.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.edu.eduplatform.course.domain.Course;
import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

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

        assertThat(courseRepository.search(null, null)).hasSize(3);
        assertThat(courseRepository.search(MemberType.ADULT, null)).hasSize(2);
        assertThat(courseRepository.search(null, EnglishLevel.BEGINNER)).hasSize(2);
        assertThat(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER))
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

        assertThat(courseRepository.search(MemberType.ADULT, EnglishLevel.BEGINNER))
                .extracting(Course::getTitle)
                .containsExactly("공식 코스");
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
