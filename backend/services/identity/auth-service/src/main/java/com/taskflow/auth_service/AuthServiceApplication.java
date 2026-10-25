package com.taskflow.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the credential-account service: registration, login, logout, and CSRF materialization.
 * Its account IDs become canonical JWT subjects used by user profiles, project membership,
 * and task assignments. application.yaml imports service configuration; controllers, security,
 * and repositories below this package implement the HTTP/authentication boundaries.
 */
@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
