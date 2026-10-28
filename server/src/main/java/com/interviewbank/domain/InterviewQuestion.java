package com.interviewbank.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "interview_question")
@Getter
@Setter
@NoArgsConstructor
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false)
    @JsonIgnore
    private InterviewRecord record;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(length = 200000)
    private String note;

    private Integer orderIndex = 0;

    private Instant createdAt = Instant.now();

    public InterviewQuestion(InterviewRecord record, Question question, String note, int orderIndex) {
        this.record = record;
        this.question = question;
        this.note = note;
        this.orderIndex = orderIndex;
    }
}
