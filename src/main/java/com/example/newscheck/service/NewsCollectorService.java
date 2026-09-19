package com.example.newscheck.service;

import com.example.newscheck.domain.NewsArticle;
import com.example.newscheck.repository.NewsArticleRepository;
import com.example.newscheck.service.dto.NaverNewsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsCollectorService {

    public static final String CATEGORY_FOCUS = "focus";
    public static final String CATEGORY_GENERAL = "general";
    public static final int RETENTION_DAYS = 7;

    private static final ZoneId NEWS_ZONE = ZoneId.of("Asia/Seoul");

    public static final String CATEGORY_DEPOSIT = "deposit";
    public static final String CATEGORY_REAL_ESTATE = "realestate";

    private static final Map<String, List<String>> KEYWORDS_BY_CATEGORY = Map.of(
            CATEGORY_FOCUS, List.of("빅테크", "급등주", "SK하이닉스", "삼성전자"),
            CATEGORY_GENERAL, List.of("코스피", "코스닥", "증시", "주식시장", "주식"),
            CATEGORY_DEPOSIT, List.of("예금", "적금", "예금금리", "기준금리"),
            CATEGORY_REAL_ESTATE, List.of("부동산", "아파트", "청약", "전세")
    );

    private final NaverNewsClient naverNewsClient;
    private final NewsArticleRepository repository;

    @Scheduled(initialDelayString = "PT0S", fixedRateString = "${newscheck.collect.interval:PT1H}")
    public void collectNews() {
        log.info("금융 뉴스 수집을 시작합니다.");

        LocalDateTime cutoff = LocalDateTime.now(NEWS_ZONE).minusDays(RETENTION_DAYS);
        int deletedCount = repository.deleteByPubDateBefore(cutoff);
        log.info("{}일이 지난 기사를 삭제했습니다. 삭제 건수: {}", RETENTION_DAYS, deletedCount);

        int savedCount = 0;

        for (Map.Entry<String, List<String>> entry : KEYWORDS_BY_CATEGORY.entrySet()) {
            String category = entry.getKey();
            for (String keyword : entry.getValue()) {
                List<NaverNewsResponse.Item> items = naverNewsClient.searchNews(keyword);
                for (NaverNewsResponse.Item item : items) {
                    if (saveIfAbsent(item, keyword, category, cutoff)) {
                        savedCount++;
                    }
                }
            }
        }

        log.info("금융 뉴스 수집을 완료했습니다. 신규 저장 건수: {}", savedCount);
    }

    private boolean saveIfAbsent(NaverNewsResponse.Item item, String keyword, String category, LocalDateTime cutoff) {
        if (item.link() == null || repository.existsByLink(item.link())) {
            return false;
        }

        LocalDateTime pubDate = parsePubDate(item.pubDate());
        if (pubDate.isBefore(cutoff)) {
            return false;
        }

        NewsArticle article = new NewsArticle();
        article.setTitle(cleanHtml(item.title()));
        article.setLink(item.link());
        article.setOriginalLink(item.originallink());
        article.setDescription(cleanHtml(item.description()));
        article.setSource(extractSource(item.originallink() != null ? item.originallink() : item.link()));
        article.setKeyword(keyword);
        article.setCategory(category);
        article.setPubDate(pubDate);
        article.setCollectedAt(LocalDateTime.now());

        repository.save(article);
        return true;
    }

    private String cleanHtml(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("<b>", "")
                .replace("</b>", "")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&#39;", "'");
    }

    private String extractSource(String url) {
        if (url == null) {
            return "알 수 없음";
        }
        try {
            String host = URI.create(url).getHost();
            return host == null ? "알 수 없음" : host.replaceFirst("^www\\.", "");
        } catch (IllegalArgumentException e) {
            return "알 수 없음";
        }
    }

    private LocalDateTime parsePubDate(String pubDate) {
        if (pubDate == null) {
            return LocalDateTime.now();
        }
        try {
            return ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime();
        } catch (DateTimeException e) {
            return LocalDateTime.now();
        }
    }
}
