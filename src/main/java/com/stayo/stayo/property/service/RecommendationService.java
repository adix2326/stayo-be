package com.stayo.stayo.property.service;

import com.stayo.stayo.property.entity.PG;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    private final MongoTemplate mongoTemplate;

    public List<PG> getRecommendedPGs(String userId) {
        log.info("Fetching recommended PGs for user: {}", userId);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("isActive").is(true)),
                Aggregation.sample(10)
        );

        AggregationResults<PG> results = mongoTemplate.aggregate(aggregation, "properties", PG.class);
        return results.getMappedResults();
    }
}
