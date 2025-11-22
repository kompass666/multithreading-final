package com.example.multithreading_final.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService crawlerExecutor() {
        // Пока просто пул на 4 потока, потом можно будет настроить
        return Executors.newFixedThreadPool(4);
    }
}
