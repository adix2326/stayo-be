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
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final SmsService smsService;
    private final MongoTemplate mongoTemplate;

    @Value("${otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${otp.max-attempts:3}")
    private int maxAttempts;

    @Value("${otp.static-code:123456}")
    private String staticOtpCode;

    @Value("${otp.use-static:true}")
    private boolean useStaticOtp;

    private final SecureRandom random = new SecureRandom();

    public void sendOtpToPhone(String mobileNumber) {
        otpRepository.deleteByMobileNumber(mobileNumber);

        String otp = useStaticOtp ? staticOtpCode : generateOtp();
        OtpRequest otpRequest = OtpRequest.builder()
                .mobileNumber(mobileNumber)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiryAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .attempts(0)
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

    public void verifyOtp(String mobileNumber, String otp) {
        // Count the attempt atomically up front, so parallel guesses can't
        // each read a stale `attempts` and slip past the lockout.
        OtpRequest otpRequest = mongoTemplate.findAndModify(
                query(where("mobileNumber").is(mobileNumber)),
                new Update().inc("attempts", 1),
                FindAndModifyOptions.options().returnNew(true),
                OtpRequest.class);
        if (otpRequest == null) {
            throw new OtpNotFoundException("OTP not found for this phone number");
        }

        if (LocalDateTime.now().isAfter(otpRequest.getExpiryAt())) {
            otpRepository.delete(otpRequest);
            log.warn("OTP expired for phone: {}", mobileNumber);
            throw new OtpExpiredException("OTP expired. Please request a new one.");
        }

        boolean matches = MessageDigest.isEqual(
                otpRequest.getOtp().getBytes(StandardCharsets.UTF_8), otp.getBytes(StandardCharsets.UTF_8));

        if (matches && otpRequest.getAttempts() <= maxAttempts) {
            // Single-use: only the request that actually removes the row wins,
            // so one OTP can't sign in twice. Deleting also frees the unique
            // mobileNumber index for the next send.
            if (mongoTemplate.findAndRemove(query(where("_id").is(otpRequest.getId())), OtpRequest.class) == null) {
                throw new OtpNotFoundException("OTP not found for this phone number");
            }
            log.info("OTP verified successfully for phone: {}", mobileNumber);
            return;
        }

        int remainingAttempts = maxAttempts - otpRequest.getAttempts();
        if (remainingAttempts <= 0) {
            otpRepository.delete(otpRequest);
            log.warn("Maximum OTP attempts exceeded for phone: {}", mobileNumber);
            throw new MaxOtpAttemptsExceededException("Maximum OTP attempts exceeded. Request a new OTP.");
        }
        log.warn("Invalid OTP attempt for phone: {}. Remaining attempts: {}", mobileNumber, remainingAttempts);
        throw new InvalidOtpException("Invalid OTP. " + remainingAttempts + " attempts remaining.");
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }
}
