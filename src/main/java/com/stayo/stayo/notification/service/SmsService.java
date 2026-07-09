package com.stayo.stayo.notification.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        log.info("Initializing Twilio with Account SID: {}", accountSid);
        Twilio.init(accountSid, authToken);
    }

    public void sendOtp(String phoneNumber, String otp) {
        try {
            log.info("Sending OTP via Twilio to {}", phoneNumber);
            Message message = Message.creator(
                            new PhoneNumber(phoneNumber), // To
                            new PhoneNumber(twilioPhoneNumber), // From
                            "Your StayO verification code is: " + otp + ". Valid for 5 minutes. Do not share this code."
                    )
                    .create();

            log.info("OTP sent successfully to {}: {}", phoneNumber, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send OTP via SMS: " + e.getMessage(), e);
        }
    }
}