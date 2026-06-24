package com.stayo.stayo.user.repository;

import com.stayo.stayo.user.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User>  findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByMobileNumber(String mobileNumber);
    boolean existsByMobileNumber(String mobileNumber);
}
