package com.stayo.stayo.owner.repository;

import com.stayo.stayo.owner.entity.OwnerProfile;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OwnerProfileRepository extends MongoRepository<OwnerProfile, String> {
    Optional<OwnerProfile> findByUserId(String userId);
}
