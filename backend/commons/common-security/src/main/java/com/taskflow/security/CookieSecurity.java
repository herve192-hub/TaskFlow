package com.taskflow.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

public final class CookieSecurity {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private CookieSecurity() { }

    public static String accessCookieName(boolean secure) {
        return secure ? "__Host-taskflow-access" : "taskflow_access";
    }

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

    public static void configure(CsrfConfigurer<HttpSecurity> csrf, boolean secure) {
        var repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieName(secure ? "__Host-XSRF-TOKEN" : "XSRF-TOKEN");
        repository.setCookieCustomizer(cookie -> cookie.path("/").sameSite("Strict").secure(secure));
        // The nonce is read from a cookie and sent as a header, never rendered in HTML.
        csrf.csrfTokenRepository(repository)
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .requireCsrfProtectionMatcher(request -> {
                    String authorization = request.getHeader("Authorization");
                    return !SAFE_METHODS.contains(request.getMethod())
                            && !(authorization != null && authorization.startsWith("Bearer "));
                });
    }
}
