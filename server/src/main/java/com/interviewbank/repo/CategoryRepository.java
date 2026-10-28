package com.interviewbank.repo;

import com.interviewbank.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderBySortOrderAscIdAsc();

    boolean existsByNameIgnoreCase(String name);

    Category findByName(String name);

    List<Category> findByDemoTrue();
}
