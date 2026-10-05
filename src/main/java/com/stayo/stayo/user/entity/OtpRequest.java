package com.stayo.stayo.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "otp_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRequest {
    @Id
    private String id;

    @Indexed(unique = true)
    private String mobileNumber;
    private String otp;
    private LocalDateTime createdAt;

    @Indexed(expireAfter = "1h")
    private LocalDateTime expiryAt;
    private int attempts;
    private boolean verified;
}
