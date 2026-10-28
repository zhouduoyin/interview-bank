package com.interviewbank.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 20)
    private String color = "#a2762a";

    @Column(length = 120)
    private String description;

    private Integer sortOrder = 0;

    private boolean demo = false;

    private Instant createdAt = Instant.now();

    public Category(String name, String color, String description, int sortOrder) {
        this.name = name;
        this.color = color;
        this.description = description;
        this.sortOrder = sortOrder;
    }
}
