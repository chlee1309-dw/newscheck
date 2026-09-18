package com.example.newscheck.service;

import com.example.newscheck.config.NaverApiProperties;
import com.example.newscheck.service.dto.NaverNewsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverNewsClient {

    private static final int DISPLAY_COUNT = 50;

    private final RestClient naverRestClient;
    private final NaverApiProperties properties;
    private final ObjectMapper objectMapper;

    public List<NaverNewsResponse.Item> searchNews(String keyword) {
        if (properties.clientId() == null || properties.clientId().isBlank()) {
            log.warn("네이버 API 자격증명이 설정되지 않아 뉴스 수집을 건너뜁니다.");
            return List.of();
        }

        try {
            String body = naverRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/v1/news")
                            .queryParam("query", keyword)
                            .queryParam("display", DISPLAY_COUNT)
                            .queryParam("sort", "date")
                            .build())
                    .header("X-NCP-APIGW-API-KEY-ID", properties.clientId())
                    .header("X-NCP-APIGW-API-KEY", properties.clientSecret())
                    .retrieve()
                    .body(String.class);

            if (body == null || body.isBlank()) {
                return List.of();
            }

            NaverNewsResponse response = objectMapper.readValue(body, NaverNewsResponse.class);
            return response.items() == null ? List.of() : response.items();
        } catch (Exception e) {
            log.error("네이버 뉴스 API 호출 실패 (keyword={})", keyword, e);
            return List.of();
        }
    }
}
