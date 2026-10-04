package com.stayo.stayo.owner.repository;

import com.stayo.stayo.owner.entity.OwnerProfile;
import com.stayo.stayo.owner.enums.VerificationStatus;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerProfileRepository extends MongoRepository<OwnerProfile, String> {
    Optional<OwnerProfile> findByUserId(String userId);

    List<OwnerProfile> findByVerificationStatus(VerificationStatus verificationStatus);
}
