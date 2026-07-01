package com.stayo.stayo.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;

@Service
@Slf4j
public class SmsService {

    @Value("${sms.gateway.provider:cloud}")
    private String provider;

    @Value("${sms.gateway.local.url:http://10.121.100.178:8080/message}")
    private String localUrl;

    @Value("${sms.gateway.local.username:sms}")
    private String localUsername;

    @Value("${sms.gateway.local.password:eNdUOw9H}")
    private String localPassword;

    @Value("${sms.gateway.cloud.url:https://api.sms-gate.app/3rdparty/v1/messages}")
    private String cloudUrl;

    @Value("${sms.gateway.cloud.username:}")
    private String cloudUsername;

    @Value("${sms.gateway.cloud.password:}")
    private String cloudPassword;

    @Value("${sms.gateway.cloud.device-id:}")
    private String cloudDeviceId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendOtp(String phoneNumber, String otp) {
        boolean isCloud = "cloud".equalsIgnoreCase(provider);
        String targetUrl = isCloud ? cloudUrl : localUrl;
        String username = isCloud ? cloudUsername : localUsername;
        String password = isCloud ? cloudPassword : localPassword;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (username != null && !username.isEmpty()) {
                String auth = username + ":" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                headers.set("Authorization", "Basic " + encodedAuth);
            }

            // Construct payload:
            // {
            //   "textMessage": { "text": "StayO OTP code: 123456" },
            //   "phoneNumbers": ["+91XXXXXXXXXX"],
            //   "deviceId": "MiCjpLO_kh7Ay9sT_TNdF" (for cloud) OR "simNumber": 1 (for local)
            // }
            Map<String, Object> textMessage = new HashMap<>();
            textMessage.put("text", "StayO OTP code: " + otp);

            Map<String, Object> payload = new HashMap<>();
            payload.put("textMessage", textMessage);
            payload.put("phoneNumbers", Collections.singletonList(phoneNumber));

            if (isCloud) {
                if (cloudDeviceId != null && !cloudDeviceId.isEmpty()) {
                    payload.put("deviceId", cloudDeviceId);
                }
            } else {
                payload.put("simNumber", 1);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            log.info("Sending SMS OTP to {} via {} gateway {}", phoneNumber, provider, targetUrl);
            restTemplate.postForEntity(targetUrl, request, String.class);
            log.info("SMS OTP sent successfully to {}", phoneNumber);

        } catch (Exception e) {
            log.error("Failed to send SMS via {} gateway to {}: {}", provider, phoneNumber, e.getMessage());
            // Fallback: log to console so the flow doesn't break if phone is disconnected
            log.info("=================================================");
            log.info("FALLBACK SMS OTP REQUEST FOR TESTING");
            log.info("Phone Number: {}", phoneNumber);
            log.info("OTP Code: {}", otp);
            log.info("=================================================");
        }
    }
}