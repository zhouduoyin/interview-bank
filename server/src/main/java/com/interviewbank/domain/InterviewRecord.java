package com.interviewbank.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interview_record")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InterviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String company;

    @Column(length = 120)
    private String position;

    @Column(length = 40)
    private String round;

    private LocalDate interviewDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InterviewResult result = InterviewResult.PENDING;

    @Column(length = 40)
    private String channel;

    @Column(length = 60)
    private String salaryRange;

    /** 面试复盘，Markdown */
    @Column(length = 200000)
    private String summary;

    /** 整体自评 1-5 */
    private Integer selfRating;

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orderIndex ASC, id ASC")
    private List<InterviewQuestion> questions = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    private boolean demo = false;

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    @Transient
    public int getQuestionCount() {
        return questions == null ? 0 : questions.size();
    }
}
