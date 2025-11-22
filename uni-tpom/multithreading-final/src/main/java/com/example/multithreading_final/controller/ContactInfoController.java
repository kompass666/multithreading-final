package com.example.multithreading_final.controller;

import com.example.multithreading_final.model.ContactInfo;
import com.example.multithreading_final.repository.ContactInfoRepository;
import com.example.multithreading_final.service.CrawlerService;
import com.example.multithreading_final.service.HtmlContactParser;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ContactInfoController {

    private final ContactInfoRepository contactInfoRepository;
    private final CrawlerService crawlerService;

    public ContactInfoController(ContactInfoRepository contactInfoRepository,
                                 CrawlerService crawlerService) {
        this.contactInfoRepository = contactInfoRepository;
        this.crawlerService = crawlerService;
    }

    // ==== получение контактов с фильтрами и parallelStream ====
    @GetMapping("/contacts")
    public List<ContactInfo> getAllContacts(
            @RequestParam(required = false) Boolean hasEmail,
            @RequestParam(required = false) Boolean hasPhone
    ) {
        return contactInfoRepository.findAll()
                .parallelStream()
                .filter(info -> {
                    if (hasEmail != null) {
                        if (hasEmail && info.getEmail() == null) return false;
                        if (!hasEmail && info.getEmail() != null) return false;
                    }
                    if (hasPhone != null) {
                        if (hasPhone && info.getPhone() == null) return false;
                        if (!hasPhone && info.getPhone() != null) return false;
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    if (a.getCrawledAt() == null && b.getCrawledAt() == null) return 0;
                    if (a.getCrawledAt() == null) return 1;
                    if (b.getCrawledAt() == null) return -1;
                    return b.getCrawledAt().compareTo(a.getCrawledAt());
                })
                .toList();
    }

    // ==== запуск краулера ====
    public record CrawlRequest(List<String> urls) {}

    @PostMapping("/crawl")
    public String startCrawl(@RequestBody CrawlRequest request) {
        crawlerService.crawlUrls(request.urls());
        return "Crawl started for " + request.urls().size() + " urls";
    }

    // ==== debug-эндпоинт для проверки парсера ====
    @GetMapping("/debug/test-parse")
    public ContactInfo testParse() {
        String html = """
                <html>
                  <body>
                    <h1>Test Company</h1>
                    <p>Телефон: +7 (999) 123-45-67</p>
                    <p>Email: test.company@example.com</p>
                    <p>Адрес: ул. Пушкина, д. 10</p>
                  </body>
                </html>
                """;

        HtmlContactParser.ParsedResult parsed =
                HtmlContactParser.parse(html, "http://test.local");

        ContactInfo info = new ContactInfo();
        info.setSourceUrl("http://test.local");
        info.setPageUrl("http://test.local");
        info.setEmail(parsed.getEmail());
        info.setPhone(parsed.getPhone());
        info.setAddress(parsed.getAddress());
        info.setCrawledAt(LocalDateTime.now());

        contactInfoRepository.save(info);

        return info;
    }
}
