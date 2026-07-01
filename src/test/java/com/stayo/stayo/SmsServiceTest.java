package com.stayo.stayo;

import com.stayo.stayo.common.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class SmsServiceTest {

    @Test
    public void testSendOtp() {
        SmsService smsService = new SmsService();
        
        // Inject gateway configuration properties
        ReflectionTestUtils.setField(smsService, "provider", "cloud");
        ReflectionTestUtils.setField(smsService, "cloudUrl", "https://api.sms-gate.app/3rdparty/v1/messages");
        ReflectionTestUtils.setField(smsService, "cloudUsername", "Q8QCCR");
        ReflectionTestUtils.setField(smsService, "cloudPassword", "kbxzx0t0off3gm");
        ReflectionTestUtils.setField(smsService, "cloudDeviceId", "MiCjpLO_kh7Ay9sT_TNdF");

        String testPhoneNumber = "+919405174247"; 
        String testOtp = "777888";

        System.out.println("=========================================");
        System.out.println("STARTING SMS GATEWAY TEST");
        System.out.println("Target Phone: " + testPhoneNumber);
        System.out.println("OTP Code: " + testOtp);
        System.out.println("=========================================");
        
        smsService.sendOtp(testPhoneNumber, testOtp);
        
        System.out.println("=========================================");
        System.out.println("SMS GATEWAY TEST FINISHED");
        System.out.println("=========================================");
    }
}
