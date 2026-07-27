package com.stayo.stayo.property.repository;

import com.stayo.stayo.property.entity.PGImages;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PGImagesRepository extends MongoRepository<PGImages, String> {

    Optional<PGImages> findByPgId(String pgId);

    List<PGImages> findByPgIdIn(List<String> pgIds);
}
