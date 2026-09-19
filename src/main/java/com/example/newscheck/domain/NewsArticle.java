package com.example.newscheck.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "news_article", uniqueConstraints = @UniqueConstraint(columnNames = "link"))
@Getter
@Setter
@NoArgsConstructor
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 500, unique = true)
    private String link;

    @Column(length = 500)
    private String originalLink;

    @Column(length = 2000)
    private String description;

    @Column(length = 200)
    private String source;

    @Column(length = 50)
    private String keyword;

    @Column(nullable = false, length = 20)
    private String category;

    private LocalDateTime pubDate;

    @Column(nullable = false)
    private LocalDateTime collectedAt;

    /** collectedAt은 서버 기본 시간대의 LocalDateTime.now()로 저장되므로 같은 시간대로 해석해 절대 시각으로 변환한다. */
    @Transient
    public long getCollectedAtEpochMillis() {
        return collectedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
