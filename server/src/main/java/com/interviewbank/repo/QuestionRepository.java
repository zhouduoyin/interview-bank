package com.interviewbank.repo;

import com.interviewbank.domain.Category;
import com.interviewbank.domain.Mastery;
import com.interviewbank.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    @Query("""
            select q from Question q
            where (:keyword is null or lower(q.title) like :keyword
                   or lower(coalesce(q.content, '')) like :keyword
                   or lower(coalesce(q.answer, '')) like :keyword
                   or lower(coalesce(q.sourceCompany, '')) like :keyword)
              and (:categoryId is null or q.category.id = :categoryId)
              and (:difficulty is null or q.difficulty = :difficulty)
              and (:mastery is null or q.mastery = :mastery)
              and (:starred is null or q.starred = :starred)
              and (:tag is null or exists (
                    select 1 from Question q2 join q2.tags t2 where q2.id = q.id and t2 = :tag))
            """)
    org.springframework.data.domain.Page<Question> search(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("difficulty") com.interviewbank.domain.Difficulty difficulty,
            @Param("mastery") com.interviewbank.domain.Mastery mastery,
            @Param("tag") String tag,
            @Param("starred") Boolean starred,
            org.springframework.data.domain.Pageable pageable);

    List<Question> findByNextReviewAtBeforeOrderByNextReviewAtAsc(Instant now, org.springframework.data.domain.Pageable pageable);

    long countByNextReviewAtBefore(Instant now);

    long countByNextReviewAtBeforeAndReviewCount(Instant now, int reviewCount);

    long countByReviewCount(int reviewCount);

    long countByMastery(Mastery mastery);

    long countByStarredTrue();

    long countByCreatedAtAfter(Instant after);

    long countByCategoryId(Long categoryId);

    List<Question> findByDemoTrue();

    Optional<Question> findFirstByTitle(String title);

    @Modifying
    @Query("update Question q set q.category = null where q.category.id = :categoryId")
    void clearCategory(@Param("categoryId") Long categoryId);

    @Query("select t, count(q) from Question q join q.tags t group by t order by count(q) desc")
    List<Object[]> countByTag();

    @Query("select distinct t from Question q join q.tags t order by t")
    List<String> findDistinctTags();

    @Query("select q from Question q where :tag member of q.tags")
    List<Question> findByTag(@Param("tag") String tag);

    @Query("select q from Question q where q.category.id = :categoryId")
    List<Question> findByCategory(@Param("categoryId") Long categoryId);
}
