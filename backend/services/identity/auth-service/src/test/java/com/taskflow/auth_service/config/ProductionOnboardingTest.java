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

/**
 * Unit/controller tests for first-admin provisioning, no automatic account promotion, and production onboarding.
 * Mocked repositories/encoders isolate account creation; direct controller calls verify approval,
 * secure cookie shape, administrator-session preservation, and opaque-ID audit behavior.
 * These tests do not replace a real filter-chain CSRF or live database integration test.
 */
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

    @Test
    void administratorProvisioningPreservesTheAdministratorsBrowserSession() {
        AuthService service = mock(AuthService.class);
        var request = new RegisterRequest();
        when(service.register(request)).thenReturn(com.taskflow.auth_service.dto.response.AuthResponse.builder()
                .accessToken("synthetic-new-account-token").build());
        var controller = new AuthController(service, new JwtProperties());
        var admin = new UsernamePasswordAuthenticationToken("admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        var result = controller.register(request, admin);
        assertEquals(201, result.getStatusCode().value());
        assertNull(result.getHeaders().getFirst(org.springframework.http.HttpHeaders.SET_COOKIE));
        assertEquals("no-store", result.getHeaders().getCacheControl());
        verify(service).register(request);
    }

    @Test
    void productionSignInUsesASecureHostOnlyCookie() {
        var service = mock(AuthService.class);
        var request = new com.taskflow.auth_service.dto.request.LoginRequest();
        when(service.login(request)).thenReturn(com.taskflow.auth_service.dto.response.AuthResponse.builder()
                .accessToken("synthetic-access-token").build());
        var properties = new JwtProperties();
        properties.setExpiration(900_000);
        var controller = new AuthController(service, properties);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "secureCookie", true);
        String cookie = controller.login(request).getHeaders().getFirst(org.springframework.http.HttpHeaders.SET_COOKIE);
        assertNotNull(cookie);
        assertTrue(cookie.startsWith("__Host-taskflow-access="));
        for (String attribute : List.of("Secure", "HttpOnly", "SameSite=Strict", "Path=/", "Max-Age=900")) {
            assertTrue(cookie.contains(attribute), attribute);
        }
        assertFalse(cookie.contains("Domain="));
    }

    @Test
    void signOutAuditUsesTheAccountIdWithoutRecordingThePrivateEmail() {
        var logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("com.taskflow.security.audit");
        var entries = new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
        entries.start();
        logger.addAppender(entries);
        try {
            var account = com.taskflow.auth_service.security.model.CustomUserDetails.builder()
                    .id("account-opaque-1").email("private@example.test").build();
            var authentication = new UsernamePasswordAuthenticationToken(account, null, List.of());
            var controller = new AuthController(mock(AuthService.class), new JwtProperties());
            controller.logout(authentication);
            assertTrue(entries.list.stream().anyMatch(entry -> entry.getFormattedMessage().contains("actor=account-opaque-1")));
            assertTrue(entries.list.stream().noneMatch(entry -> entry.getFormattedMessage().contains("private@example.test")));
        } finally {
            logger.detachAppender(entries);
            entries.stop();
        }
    }
}
