package com.stayo.stayo.content.repository;

import com.stayo.stayo.content.entity.DashboardCategory;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DashboardCategoryRepository extends MongoRepository<DashboardCategory, String> {
    List<DashboardCategory> findAllByOrderByDisplayOrderAsc();
}
