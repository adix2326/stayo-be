package com.stayo.stayo.content.repository;

import com.stayo.stayo.content.entity.PopularSearch;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PopularSearchRepository extends MongoRepository<PopularSearch, String> {
}
