package com.stayo.stayo.auth.service;

import com.stayo.stayo.notification.service.SmsService;
import com.stayo.stayo.shared.exception.InvalidOtpException;
import com.stayo.stayo.shared.exception.MaxOtpAttemptsExceededException;
import com.stayo.stayo.shared.exception.OtpNotFoundException;
import com.stayo.stayo.user.entity.OtpRequest;
import com.stayo.stayo.user.repository.OtpRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    private static final String MOBILE = "+919876543210";

    @Mock private OtpRepository otpRepository;
    @Mock private SmsService smsService;
    @Mock private MongoTemplate mongoTemplate;

    private OtpService newService() {
        OtpService service = new OtpService(otpRepository, smsService, mongoTemplate);
        ReflectionTestUtils.setField(service, "maxAttempts", 3);
        return service;
    }

    /** What findAndModify returns: the stored OTP with `attempts` already incremented. */
    private void givenStored(String code, int attemptsAfterIncrement) {
        OtpRequest stored = OtpRequest.builder().id("id1").mobileNumber(MOBILE).otp(code)
                .attempts(attemptsAfterIncrement).expiryAt(LocalDateTime.now().plusMinutes(5)).build();
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class),
                any(FindAndModifyOptions.class), eq(OtpRequest.class))).thenReturn(stored);
    }

    @Test
    void successfulVerify_removesOtp_soNextSendIsNotBlockedByUniqueIndex() {
        givenStored("123456", 1);
        when(mongoTemplate.findAndRemove(any(Query.class), eq(OtpRequest.class))).thenReturn(new OtpRequest());

        newService().verifyOtp(MOBILE, "123456");

        verify(mongoTemplate).findAndRemove(any(Query.class), eq(OtpRequest.class));
    }

    @Test
    void parallelRequestAlreadyConsumedOtp_isRejected() {
        givenStored("123456", 1);
        when(mongoTemplate.findAndRemove(any(Query.class), eq(OtpRequest.class))).thenReturn(null);

        assertThrows(OtpNotFoundException.class, () -> newService().verifyOtp(MOBILE, "123456"));
    }

    @Test
    void wrongOtp_isRejectedAndKept() {
        givenStored("123456", 1);

        assertThrows(InvalidOtpException.class, () -> newService().verifyOtp(MOBILE, "000000"));

        verify(mongoTemplate, never()).findAndRemove(any(Query.class), eq(OtpRequest.class));
        verify(otpRepository, never()).delete(any(OtpRequest.class));
    }

    @Test
    void lastWrongAttempt_locksOut() {
        givenStored("123456", 3);

        assertThrows(MaxOtpAttemptsExceededException.class, () -> newService().verifyOtp(MOBILE, "000000"));

        verify(otpRepository).delete(any(OtpRequest.class));
    }

    @Test
    void correctOtpAfterLockout_isRejected() {
        givenStored("123456", 4); // parallel guesses pushed the counter past the limit

        assertThrows(MaxOtpAttemptsExceededException.class, () -> newService().verifyOtp(MOBILE, "123456"));

        verify(mongoTemplate, never()).findAndRemove(any(Query.class), eq(OtpRequest.class));
    }
}
