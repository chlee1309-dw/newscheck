package com.example.newscheck.web;

import com.example.newscheck.domain.NewsArticle;
import com.example.newscheck.repository.NewsArticleRepository;
import com.example.newscheck.service.NewsCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewsController {

    private final NewsArticleRepository repository;

    @GetMapping("/")
    public String focusList(Model model) {
        return renderList(model, NewsCollectorService.CATEGORY_FOCUS, "빅테크 · 주요 종목");
    }

    @GetMapping("/general")
    public String generalList(Model model) {
        return renderList(model, NewsCollectorService.CATEGORY_GENERAL, "전체 주식 뉴스");
    }

    private String renderList(Model model, String category, String pageTitle) {
        List<NewsArticle> articles = repository.findTop200ByCategoryOrderByPubDateDesc(category);
        NewsArticle latest = repository.findTopByCategoryOrderByCollectedAtDesc(category);

        model.addAttribute("articles", articles);
        model.addAttribute("lastCollectedAt", latest == null ? null : latest.getCollectedAt());
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activeCategory", category);
        return "news/list";
    }
}
