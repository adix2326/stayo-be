package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.Property;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PropertyRepository extends MongoRepository<Property, String> {
    long countByCityAndIsActiveTrue(String city);
    List<Property> findByIsActiveTrueAndIsFeaturedTrue();
}

