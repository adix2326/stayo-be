package com.stayo.stayo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class KeepAliveScheduler {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String KEEP_ALIVE_URL = "https://stayo-be.onrender.com/api/auth/otp/send";

    @Scheduled(fixedRate = 240000) // 4 minutes (240,000 milliseconds)
    public void keepAlive() {
        try {
            log.info("Sending scheduled keep-alive request to Render backend...");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("mobileNumber", "+910000000000");
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(KEEP_ALIVE_URL, request, String.class);
            log.info("Keep-alive response: {}", response);
        } catch (Exception e) {
            log.error("Failed to execute keep-alive request: {}", e.getMessage());
        }
    }
}
