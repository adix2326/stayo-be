package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.PopularSearch;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PopularSearchRepository extends MongoRepository<PopularSearch, String> {
}
