package com.example.newscheck.web;

import com.example.newscheck.domain.NewsArticle;
import com.example.newscheck.repository.NewsArticleRepository;
import com.example.newscheck.service.NewsCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Duration;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewsController {

    private final NewsArticleRepository repository;

    @Value("${newscheck.collect.interval:PT1H}")
    private Duration collectInterval;

    @GetMapping("/")
    public String focusList(Model model) {
        return renderList(model, NewsCollectorService.CATEGORY_FOCUS, "빅테크 · 주요 종목");
    }

    @GetMapping("/general")
    public String generalList(Model model) {
        return renderList(model, NewsCollectorService.CATEGORY_GENERAL, "전체 주식 뉴스");
    }

    @GetMapping("/deposit")
    public String depositList(Model model) {
        return renderList(model, NewsCollectorService.CATEGORY_DEPOSIT, "예금 · 적금 뉴스");
    }

    @GetMapping("/realestate")
    public String realEstateList(Model model) {
        return renderList(model, NewsCollectorService.CATEGORY_REAL_ESTATE, "부동산 뉴스");
    }

    private String renderList(Model model, String category, String pageTitle) {
        List<NewsArticle> articles = repository.findTop200ByCategoryOrderByPubDateDesc(category);
        NewsArticle latest = repository.findTopByCategoryOrderByCollectedAtDesc(category);

        model.addAttribute("articles", articles);
        model.addAttribute("lastCollectedAt", latest == null ? null : latest.getCollectedAt());
        // 새 기사 판단의 기준 시각. 브라우저 시계 대신 서버 시각을 써서 시계 오차의 영향을 피한다.
        model.addAttribute("serverNow", System.currentTimeMillis());
        model.addAttribute("collectIntervalHours", Math.max(1, collectInterval.toHours()));
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activeCategory", category);
        return "news/list";
    }
}
