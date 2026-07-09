package com.stayo.stayo.search.repository;

import com.stayo.stayo.search.entity.City;



import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CityRepository extends MongoRepository<City, String> {
    List<City> findByIsActiveTrueAndIsPopularTrue();
}
