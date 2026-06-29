package com.stayo.stayo.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Slf4j
public class HealthCheckPingService {

    private final HttpClient httpClient;
    private final String pingUrl;

    public HealthCheckPingService(
            @Value("${health.ping.url:http://localhost:8081/health}") String pingUrl) {
        this.pingUrl = pingUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        log.info("Initialized HealthCheckPingService with ping URL: {}", this.pingUrl);
    }

    /**
     * Scheduled task to ping the health endpoint every 14 minutes.
     * Cron expression can be configured in application.properties under health.ping.cron.
     */
    @Scheduled(cron = "${health.ping.cron:0 */14 * * * *}")
    public void pingHealthEndpoint() {
        try {
            log.info("Pinging health endpoint: {}", pingUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pingUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Health ping response code: {}, body: {}", response.statusCode(), response.body());
        } catch (Exception e) {
            log.error("Failed to ping health endpoint: {}", e.getMessage(), e);
        }
    }
}
