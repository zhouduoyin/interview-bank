package com.interviewbank.repo;

import com.interviewbank.domain.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

    List<ReviewLog> findByQuestionIdOrderByCreatedAtDesc(Long questionId);

    List<ReviewLog> findByCreatedAtAfterOrderByCreatedAtAsc(Instant after);

    long countByCreatedAtAfter(Instant after);

    @Modifying
    @Query("delete from ReviewLog r where r.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);

    long countByQuestionDemoTrue();
}
