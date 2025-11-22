package com.example.multithreading_final;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MultithreadingFinalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultithreadingFinalApplication.class, args);
    }
}
