package com.stayo.stayo.document.repository;

import com.stayo.stayo.document.entity.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentRepository extends MongoRepository<Document, String> {

    List<Document> findByUserId(String userId);
}
