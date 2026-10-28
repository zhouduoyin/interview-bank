package com.interviewbank.repo;

import com.interviewbank.domain.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    @Modifying
    @Query("delete from InterviewQuestion iq where iq.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);

    long countByQuestionId(Long questionId);

    long countByQuestionIdAndRecordDemoTrue(Long questionId);

    long countByRecordDemoTrue();

    List<InterviewQuestion> findByQuestionId(Long questionId);
}
