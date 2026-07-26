package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.PGView;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PGViewRepository extends MongoRepository<PGView, String> {
    List<PGView> findByUserId(String userId);
    long countByPropertyIdInAndViewedAtBetween(List<String> propertyIds, LocalDateTime start, LocalDateTime end);
}
