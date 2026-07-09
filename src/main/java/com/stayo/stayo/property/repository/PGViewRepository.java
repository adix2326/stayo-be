package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.PGView;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PGViewRepository extends MongoRepository<PGView, String> {
    List<PGView> findByUserId(String userId);
}
