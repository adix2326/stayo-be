package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.PG;



import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PGRepository extends MongoRepository<PG, String> {
    long countByCityAndIsActiveTrue(String city);
    List<PG> findByIsActiveTrueAndIsFeaturedTrue();
    List<PG> findByOwnerId(String ownerId);
}

