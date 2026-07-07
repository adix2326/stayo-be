package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.PropertyView;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PropertyViewRepository extends MongoRepository<PropertyView, String> {
    List<PropertyView> findByUserId(String userId);
}
