package com.interviewbank.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "question")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 200000)
    private String content;

    @Column(length = 200000)
    private String answer;

    @Column(length = 200000)
    private String note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Mastery mastery = Mastery.NEW;

    @Column(length = 120)
    private String sourceCompany;

    @Column(length = 120)
    private String sourcePosition;

    private boolean starred = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_tags", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "tag", length = 40)
    private Set<String> tags = new LinkedHashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant lastReviewedAt;

    private Instant nextReviewAt;

    private int reviewCount = 0;

    private int intervalDays = 0;

    private double easeFactor = 2.5;

    private boolean demo = false;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (nextReviewAt == null) nextReviewAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /** 列表摘要：去掉 Markdown 标记后截取一段纯文本。 */
    @Transient
    public String getExcerpt() {
        String text = content == null ? "" : content;
        text = text.replaceAll("```[\\s\\S]*?```", " [代码] ");
        text = text.replaceAll("[#>*`\\[\\]()!|\\-]", " ");
        text = text.replaceAll("\\s+", " ").trim();
        return text.length() <= 110 ? text : text.substring(0, 110) + "…";
    }
}
