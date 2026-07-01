package com.stayo.stayo.auth.repository;

import com.stayo.stayo.auth.entity.BlacklistedToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface BlacklistedTokenRepository extends MongoRepository<BlacklistedToken, String> {
    boolean existsByToken(String token);
    Optional<BlacklistedToken> findByToken(String token);
}
