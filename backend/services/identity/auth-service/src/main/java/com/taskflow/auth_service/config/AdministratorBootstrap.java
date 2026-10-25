package com.taskflow.auth_service.config;

import com.taskflow.auth_service.domain.AuthProvider;
import com.taskflow.auth_service.domain.Role;
import com.taskflow.auth_service.domain.User;
import com.taskflow.auth_service.repository.UserRepository;
import java.util.Locale;
import java.util.Set;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Creates the first production administrator from protected startup properties through ApplicationRunner.
 * If an administrator already exists it does nothing; otherwise it creates a new encoded-password
 * account and refuses to promote a colliding email/username. The initial Compose override mounts
 * these secrets into auth only; deploy/aws/taskflow.py later removes the bootstrap mounts.
 */
@Configuration
@Profile("prod")
public class AdministratorBootstrap {
    @Bean
    ApplicationRunner initialAdministrator(UserRepository users, PasswordEncoder encoder, Environment environment) {
        return ignored -> {
            // Existing administrators make subsequent starts independent of bootstrap credentials.
            if (users.hasAdministrator()) return;
            String email = environment.getProperty("taskflow.bootstrap.admin.email", "").trim().toLowerCase(Locale.ROOT);
            String password = environment.getProperty("taskflow.bootstrap.admin.password", "");
            if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || password.length() < 15 || password.length() > 128) {
                throw new IllegalStateException("Provision the first administrator using the protected bootstrap secrets");
            }
            if (users.existsByEmail(email) || users.existsByUsername("platform-admin")) {
                throw new IllegalStateException("Bootstrap identity already exists; an existing account will not be promoted");
            }
            users.save(User.builder().username("platform-admin").email(email)
                    .firstName("Platform").lastName("Administrator").password(encoder.encode(password))
                    .roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN)).provider(AuthProvider.LOCAL)
                    .enabled(true).emailVerified(true).accountNonLocked(true)
                    .accountNonExpired(true).credentialsNonExpired(true).build());
        };
    }
}
