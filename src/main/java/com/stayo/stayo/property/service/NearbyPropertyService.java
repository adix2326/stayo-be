package com.stayo.stayo.property.service;

import com.stayo.stayo.property.entity.Property;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NearbyPropertyService {
    private final MongoTemplate mongoTemplate;

    public List<Property> getNearbyProperties(String city) {
        log.info("Fetching nearby properties for city: {}", city);
        
        Query query = new Query();
        if (city != null) {
            query.addCriteria(Criteria.where("city").regex("^" + city.trim() + "$", "i"));
        }
        query.addCriteria(Criteria.where("isActive").is(true));
        query.limit(10);
        
        return mongoTemplate.find(query, Property.class);
    }
}
