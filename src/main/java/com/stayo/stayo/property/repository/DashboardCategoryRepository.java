package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.DashboardCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DashboardCategoryRepository extends MongoRepository<DashboardCategory, String> {
    List<DashboardCategory> findAllByOrderByDisplayOrderAsc();
}
