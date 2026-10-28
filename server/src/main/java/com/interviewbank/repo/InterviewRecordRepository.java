package com.interviewbank.repo;

import com.interviewbank.domain.InterviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewRecordRepository extends JpaRepository<InterviewRecord, Long> {

    List<InterviewRecord> findAllByOrderByInterviewDateDescIdDesc();

    List<InterviewRecord> findTop5ByOrderByInterviewDateDescIdDesc();

    List<InterviewRecord> findByDemoTrue();

    long countByDemoTrue();
}
