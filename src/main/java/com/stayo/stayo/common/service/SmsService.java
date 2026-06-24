package com.stayo.stayo.common.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

//    public void sendOtp(String phoneNumber, String otp) {
//        try {
//            Twilio.init(accountSid, authToken);
//
//            Message message = Message.creator(
//                            new PhoneNumber(twilioPhoneNumber),
//                            new PhoneNumber(phoneNumber)
//                    )
//                    .setBody("Your StayO verification code is: " + otp + ". Valid for 5 minutes. Do not share this code.")
//                    .create();
//
//            log.info("OTP sent successfully to {}: {}", phoneNumber, message.getSid());
//        } catch (Exception e) {
//            log.error("Failed to send OTP to {}: {}", phoneNumber, e.getMessage());
//            throw new RuntimeException("Failed to send OTP via SMS: " + e.getMessage(), e);
//        }
//    }

    public void sendOtp(String phoneNumber, String otp) {
        // Static OTP for development
        log.info("=================================================");
        log.info("OTP REQUEST FOR TESTING");
        log.info("Phone Number: {}", phoneNumber);
        log.info("OTP Code: {}", otp);
        log.info("Valid for 5 minutes");
        log.info("=================================================");
    }
}