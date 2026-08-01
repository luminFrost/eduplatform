package com.edu.eduplatform.question.repository;

import com.edu.eduplatform.member.domain.EnglishLevel;
import com.edu.eduplatform.member.domain.MemberType;
import com.edu.eduplatform.question.domain.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * options(@ElementCollection)를 fetch join으로 함께 가져와, 문항마다 따로 나가던
     * question_option 추가 select N번을 없앤다. @OrderColumn 순서는 JOIN FETCH에서도 보존된다.
     */
    @Query("select distinct q from Question q left join fetch q.options "
            + "where q.targetType = :targetType and q.level = :level")
    List<Question> findByTargetTypeAndLevel(@Param("targetType") MemberType targetType, @Param("level") EnglishLevel level);
}
