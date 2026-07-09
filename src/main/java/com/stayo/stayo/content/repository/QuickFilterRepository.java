package com.stayo.stayo.content.repository;

import com.stayo.stayo.content.entity.QuickFilter;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface QuickFilterRepository extends MongoRepository<QuickFilter, String> {
    List<QuickFilter> findAllByOrderByDisplayOrderAsc();
}
