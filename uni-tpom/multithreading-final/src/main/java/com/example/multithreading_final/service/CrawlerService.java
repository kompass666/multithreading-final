package com.example.multithreading_final.service;

import com.example.multithreading_final.model.ContactInfo;
import com.example.multithreading_final.repository.ContactInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@Service
public class CrawlerService {

    private final ExecutorService executorService;
    private final ContactInfoRepository contactInfoRepository;
    private final RestTemplate restTemplate;

    public CrawlerService(ExecutorService executorService,
                          ContactInfoRepository contactInfoRepository) {
        this.executorService = executorService;
        this.contactInfoRepository = contactInfoRepository;
        this.restTemplate = new RestTemplate(); // на первое время достаточно
    }

    public void crawlUrls(List<String> startUrls) {
        for (String url : startUrls) {
            executorService.submit(() -> crawlSinglePage(url, url));
        }
    }

    private void crawlSinglePage(String pageUrl, String sourceUrl) {
        try {
            String body = restTemplate.getForObject(pageUrl, String.class);
            if (body == null) {
                System.err.println("Empty response from " + pageUrl);
                return;
            }

            // Парсим html
            HtmlContactParser.ParsedResult parsed = HtmlContactParser.parse(body, pageUrl);

            // Сохраняем контакты по самой странице
            ContactInfo info = new ContactInfo();
            info.setSourceUrl(sourceUrl);
            info.setPageUrl(pageUrl);
            info.setCrawledAt(LocalDateTime.now());
            info.setEmail(parsed.getEmail());
            info.setPhone(parsed.getPhone());
            info.setAddress(parsed.getAddress());
            info.setOrganizationName(null); // потом можно отдельно вытащить из title/h1

            contactInfoRepository.save(info);

            // На будущее: можно также обходить links в parsed.getLinks()
            // и запускать crawlSinglePage для них (с ограничением по глубине).
            Set<String> links = parsed.getLinks();
            System.out.println("Found " + links.size() + " links on " + pageUrl);

        } catch (Exception e) {
            System.err.println("Failed to crawl " + pageUrl + ": " + e.getMessage());
        }
    }
}