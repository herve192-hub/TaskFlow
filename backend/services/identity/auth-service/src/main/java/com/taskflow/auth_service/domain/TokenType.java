// 
package com.taskflow.auth_service.domain;

/**
 * Names the access/refresh distinction for account-domain callers.
 * JwtServiceImpl currently places lowercase token_use claims and distinct audiences in JWTs;
 * AccessTokenValidator rejects refresh credentials at business APIs independently of this enum.
 */
public enum TokenType {
    ACCESS,

    REFRESH
}
