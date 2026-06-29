package com.stayo.stayo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StayOApplication {

    public static void main(String[] args) {
        SpringApplication.run(StayOApplication.class, args);
    }

}

