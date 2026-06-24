package com.stayo.stayo.auth.service;

import com.stayo.stayo.common.exception.InvalidOtpException;
import com.stayo.stayo.common.exception.MaxOtpAttemptsExceededException;
import com.stayo.stayo.common.exception.OtpExpiredException;
import com.stayo.stayo.common.exception.OtpNotFoundException;
import com.stayo.stayo.user.entity.OtpRequest;
import com.stayo.stayo.user.repository.OtpRepository;
import com.stayo.stayo.common.service.SmsService;
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

    private final Random random = new Random();

    public void sendOtpToPhone(String mobileNumber) {
        otpRepository.deleteByMobileNumberAndVerifiedFalse(mobileNumber);

//        String otp = generateOtp();
        OtpRequest otpRequest = OtpRequest.builder()
                .mobileNumber(mobileNumber)
//                .otp(otp)
                .otp(staticOtpCode)
                .createdAt(LocalDateTime.now())
                .expiryAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .attempts(0)
                .verified(false)
                .build();

        otpRepository.save(otpRequest);
//        smsService.sendOtp(mobileNumber, otp);
        log.info("OTP sent to phone: {}", mobileNumber);

        // Log instead of sending SMS (for development)
        log.info("=================================================");
        log.info("OTP REQUEST FOR TESTING");
        log.info("Phone Number: {}", mobileNumber);
        log.info("OTP Code: {}", staticOtpCode);
        log.info("Valid for {} minutes", otpExpiryMinutes);
        log.info("=================================================");
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