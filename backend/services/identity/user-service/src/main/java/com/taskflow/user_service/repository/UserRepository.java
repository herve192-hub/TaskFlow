package com.taskflow.user_service.repository;

import com.taskflow.user_service.domain.User;
import com.taskflow.user_service.domain.enums.UserStatus;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends MongoRepository<User, String> {

    Optional<User> findByAuthUserId(
            String authUserId
    );

    Optional<User> findByEmail(
            String email
    );

    boolean existsByAuthUserId(
            String authUserId
    );

    boolean existsByEmail(
            String email
    );

    List<User> findByStatus(
            UserStatus status
    );
}