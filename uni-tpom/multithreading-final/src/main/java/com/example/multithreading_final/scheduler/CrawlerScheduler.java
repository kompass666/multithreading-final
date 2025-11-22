package com.example.multithreading_final.scheduler;

import com.example.multithreading_final.service.CrawlerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrawlerScheduler {

    private final CrawlerService crawlerService;

    public CrawlerScheduler(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    // каждые 5 минут после окончания предыдущего запуска
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void scheduledCrawl() {
        System.out.println(">>> scheduledCrawl started");
        crawlerService.crawlUrls(
                List.of(
                        "https://example.com"
                        // сюда можешь добавить свои стартовые URL
                )
        );
    }
}
