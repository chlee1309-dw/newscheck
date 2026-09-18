package com.example.newscheck.repository;

import com.example.newscheck.domain.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    boolean existsByLink(String link);

    List<NewsArticle> findTop200ByCategoryOrderByPubDateDesc(String category);

    NewsArticle findTopByCategoryOrderByCollectedAtDesc(String category);
}
