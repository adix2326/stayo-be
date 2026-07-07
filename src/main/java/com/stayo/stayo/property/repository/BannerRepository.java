package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.Banner;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BannerRepository extends MongoRepository<Banner, String> {
    List<Banner> findAllByOrderByDisplayOrderAsc();
}
