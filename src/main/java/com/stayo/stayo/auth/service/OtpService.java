package com.stayo.stayo.auth.service;

import com.stayo.stayo.notification.service.SmsService;
import com.stayo.stayo.shared.exception.InvalidOtpException;
import com.stayo.stayo.shared.exception.MaxOtpAttemptsExceededException;
import com.stayo.stayo.shared.exception.OtpExpiredException;
import com.stayo.stayo.shared.exception.OtpNotFoundException;
import com.stayo.stayo.user.entity.OtpRequest;
import com.stayo.stayo.user.repository.OtpRepository;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final SmsService smsService;

    @Value("${otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${otp.max-attempts:3}")
    private int maxAttempts;

    @Value("${otp.static-code:123456}")
    private String staticOtpCode;

    @Value("${otp.use-static:true}")
    private boolean useStaticOtp;

    private final Random random = new Random();

    public void sendOtpToPhone(String mobileNumber) {
        otpRepository.deleteByMobileNumberAndVerifiedFalse(mobileNumber);

        String otp = useStaticOtp ? staticOtpCode : generateOtp();
        OtpRequest otpRequest = OtpRequest.builder()
                .mobileNumber(mobileNumber)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiryAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .attempts(0)
                .verified(false)
                .build();

        otpRepository.save(otpRequest);

        if (useStaticOtp || "+910000000000".equals(mobileNumber)) {
            log.info("Using static OTP for testing/keep-alive: {}", mobileNumber);
            log.info("=================================================");
            log.info("OTP REQUEST FOR TESTING");
            log.info("Phone Number: {}", mobileNumber);
            log.info("Static OTP Code: {}", staticOtpCode);
            log.info("Valid for {} minutes", otpExpiryMinutes);
            log.info("=================================================");
        } else {
            smsService.sendOtp(mobileNumber, otp);
            log.info("OTP sent to phone: {}", mobileNumber);
        }
    }

    public boolean verifyOtp(String mobileNumber, String otp) {
        OtpRequest otpRequest = otpRepository.findByMobileNumberAndVerifiedFalse(mobileNumber)
                .orElseThrow(() -> new OtpNotFoundException("OTP not found for this phone number"));

        if (LocalDateTime.now().isAfter(otpRequest.getExpiryAt())) {
            otpRepository.delete(otpRequest);
            log.warn("OTP expired for phone: {}", mobileNumber);
            throw new OtpExpiredException("OTP expired. Please request a new one.");
        }

        if (!otpRequest.getOtp().equals(otp)) {
            otpRequest.setAttempts(otpRequest.getAttempts() + 1);
            if (otpRequest.getAttempts() >= maxAttempts) {
                otpRepository.delete(otpRequest);
                log.warn("Maximum OTP attempts exceeded for phone: {}", mobileNumber);
                throw new MaxOtpAttemptsExceededException("Maximum OTP attempts exceeded. Request a new OTP.");
            }
            otpRepository.save(otpRequest);
            int remainingAttempts = maxAttempts - otpRequest.getAttempts();
            log.warn("Invalid OTP attempt for phone: {}. Remaining attempts: {}", mobileNumber, remainingAttempts);
            throw new InvalidOtpException("Invalid OTP. " + remainingAttempts + " attempts remaining.");
        }

        otpRequest.setVerified(true);
        otpRepository.save(otpRequest);
        log.info("OTP verified successfully for phone: {}", mobileNumber);
        return true;
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }
}