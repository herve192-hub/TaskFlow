package com.taskflow.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/** Shared browser-cookie names, bearer-token extraction, and CSRF configuration. */
public final class CookieSecurity {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private CookieSecurity() { }

    /** The production prefix requires Secure, Path=/, and no Domain attribute. */
    public static String accessCookieName(boolean secure) {
        return secure ? "__Host-taskflow-access" : "taskflow_access";
    }

    /** Prefer an explicit bearer token; consult the configured browser cookie only when absent. */
    public static String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        if (request.getCookies() != null) {
            // Prefer the host-only production cookie; never fall back from a bad bearer token.
            boolean secure = Boolean.parseBoolean(System.getenv("TASKFLOW_COOKIE_SECURE"))
                    || Boolean.parseBoolean(System.getProperty("TASKFLOW_COOKIE_SECURE"));
            for (String name : new String[] {accessCookieName(secure)}) {
                for (Cookie cookie : request.getCookies()) {
                    if (name.equals(cookie.getName())) return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Require the cookie/header nonce on unsafe requests using browser authentication.
     * Credential endpoints always require the nonce; other explicit bearer requests
     * use token authentication without the cookie CSRF contract.
     */
    public static void configure(CsrfConfigurer<HttpSecurity> csrf, boolean secure) {
        // Only the CSRF nonce is JavaScript-readable; the access cookie remains HttpOnly.
        var repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieName(secure ? "__Host-XSRF-TOKEN" : "XSRF-TOKEN");
        repository.setCookieCustomizer(cookie -> cookie.path("/").sameSite("Strict").secure(secure));
        // The nonce is read from a cookie and sent as a header, never rendered in HTML.
        csrf.csrfTokenRepository(repository)
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .requireCsrfProtectionMatcher(request -> {
                    String authorization = request.getHeader("Authorization");
                    // Login and registration are reachable before bearer authentication is validated.
                    // An arbitrary Authorization header must never exempt these cookie-setting endpoints.
                    boolean establishesSession = Set.of("/auth/login", "/auth/register")
                            .contains(request.getServletPath());
                    return !SAFE_METHODS.contains(request.getMethod())
                            && (establishesSession || !(authorization != null && authorization.startsWith("Bearer ")));
                });
    }
}
