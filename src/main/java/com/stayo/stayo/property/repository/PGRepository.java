package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.PG;



import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PGRepository extends MongoRepository<PG, String> {
    long countByCityAndIsActiveTrue(String city);
    List<PG> findByIsActiveTrueAndIsFeaturedTrue();
    List<PG> findByOwnerId(String ownerId);

    /** Id + isActive only — enough for owner dashboard counts, skips the heavy PG fields. */
    List<ActivityView> findProjectedByOwnerId(String ownerId);

    interface ActivityView {
        String getId();
        Boolean getIsActive();
    }
}

