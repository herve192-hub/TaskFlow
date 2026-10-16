package com.taskflow.auth_service.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.taskflow.auth_service.domain.Role;
import com.taskflow.auth_service.dto.request.RegisterRequest;
import com.taskflow.auth_service.repository.UserRepository;
import com.taskflow.auth_service.security.jwt.JwtProperties;
import com.taskflow.auth_service.service.AuthService;
import com.taskflow.auth_service.controller.AuthController;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

class ProductionOnboardingTest {
    private MockEnvironment bootstrap() {
        return new MockEnvironment().withProperty("taskflow.bootstrap.admin.email", "admin@example.test")
                .withProperty("taskflow.bootstrap.admin.password", "synthetic-bootstrap-passphrase");
    }

    @Test
    void firstAdministratorIsCreatedWithHashedCredentialsAndExplicitRoles() throws Exception {
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("hash-for-test");
        new AdministratorBootstrap().initialAdministrator(users, encoder, bootstrap()).run(null);
        verify(users).save(argThat(user -> user.getRoles().contains(Role.ROLE_ADMIN)
                && user.isEnabled() && user.isEmailVerified() && "hash-for-test".equals(user.getPassword())));
    }

    @Test
    void anExistingAccountIsNeverAutomaticallyPromoted() {
        UserRepository users = mock(UserRepository.class);
        when(users.existsByEmail(anyString())).thenReturn(true);
        var runner = new AdministratorBootstrap().initialAdministrator(users, mock(PasswordEncoder.class), bootstrap());
        assertThrows(IllegalStateException.class, () -> runner.run(null));
        verify(users, never()).save(any());
    }

    @Test
    void initialProvisioningCannotUseMissingCredentials() {
        var users = mock(UserRepository.class);
        var runner = new AdministratorBootstrap().initialAdministrator(users, mock(PasswordEncoder.class), new MockEnvironment());
        assertThrows(IllegalStateException.class, () -> runner.run(null));
        verify(users, never()).save(any());
    }

    @Test
    void anonymousVisitorsAndOrdinaryMembersCannotProvisionAccounts() {
        AuthService service = mock(AuthService.class);
        var controller = new AuthController(service, new JwtProperties());
        assertThrows(AccessDeniedException.class, () -> controller.register(new RegisterRequest(), null));
        var member = new UsernamePasswordAuthenticationToken("member", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        assertThrows(AccessDeniedException.class, () -> controller.register(new RegisterRequest(), member));
        verifyNoInteractions(service);
    }
}
