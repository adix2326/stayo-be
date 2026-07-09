package com.stayo.stayo.content.repository;

import com.stayo.stayo.content.entity.Banner;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BannerRepository extends MongoRepository<Banner, String> {
    List<Banner> findAllByOrderByDisplayOrderAsc();
}
