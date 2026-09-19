package com.example.newscheck.repository;

import com.example.newscheck.domain.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    boolean existsByLink(String link);

    List<NewsArticle> findTop200ByCategoryOrderByPubDateDesc(String category);

    NewsArticle findTopByCategoryOrderByCollectedAtDesc(String category);

    @Modifying
    @Transactional
    @Query("delete from NewsArticle a where a.pubDate < :cutoff")
    int deleteByPubDateBefore(@Param("cutoff") LocalDateTime cutoff);
}
