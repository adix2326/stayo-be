package com.stayo.stayo.user.repository;

import com.stayo.stayo.user.entity.OtpRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpRepository extends MongoRepository<OtpRequest, String> {
    Optional<OtpRequest> findByMobileNumberAndVerifiedFalse(String mobileNumber);
    void deleteByMobileNumberAndVerifiedFalse(String mobileNumber);
}
