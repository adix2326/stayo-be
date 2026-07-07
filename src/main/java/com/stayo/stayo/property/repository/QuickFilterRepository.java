package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.QuickFilter;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface QuickFilterRepository extends MongoRepository<QuickFilter, String> {
    List<QuickFilter> findAllByOrderByDisplayOrderAsc();
}
