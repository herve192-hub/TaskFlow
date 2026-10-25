package com.taskflow.user_service.repository;

import com.taskflow.user_service.domain.User;
import com.taskflow.user_service.domain.enums.UserStatus;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Profile persistence queries scoped by document ID, canonical auth ID, email, or status.
 * searchActiveUsers accepts an escaped regular expression from UserServiceImpl and matches only
 * ACTIVE names/email; lookup-by-auth-ID supports preserving inactive historical team references.
 * Repository methods themselves do not enforce private-profile ownership.
 */
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

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    List<User> findByAuthUserIdIn(List<String> authUserIds);

    // Match email or the assembled full name; callers pass Pattern.quote(search) as the regex.
    @Query("""
            { 'status': 'ACTIVE', '$or': [
                { 'email': { '$regex': ?0, '$options': 'i' } },
                { '$expr': { '$regexMatch': {
                    'input': { '$concat': [
                        { '$ifNull': ['$firstName', ''] }, ' ',
                        { '$ifNull': ['$lastName', ''] }
                    ] },
                    'regex': ?0, 'options': 'i'
                } } }
            ] }
            """)
    Page<User> searchActiveUsers(String searchPattern, Pageable pageable);
}
