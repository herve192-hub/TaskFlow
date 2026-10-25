package com.taskflow.auth_service.repository;

import com.taskflow.auth_service.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * MongoDB access for auth's credential documents, used by AuthServiceImpl and bootstrap.
 * Email/username lookups support login and uniqueness checks; hasAdministrator gates first-start
 * provisioning. It does not access user-service profiles. Canonicalization belongs in the service,
 * and unique Mongo indexes remain important for concurrent registration attempts.
 */
public interface UserRepository
        extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @org.springframework.data.mongodb.repository.Query(value = "{roles: 'ROLE_ADMIN'}", exists = true)
    boolean hasAdministrator();

}
