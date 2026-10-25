package com.taskflow.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Starts profile, preferences, avatar, and directory APIs in the user-service package.
 * Credential validation/token issuance belongs to auth-service; profile records link to that
 * account through authUserId. Configuration selects a separate logical MongoDB database.
 */
@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
