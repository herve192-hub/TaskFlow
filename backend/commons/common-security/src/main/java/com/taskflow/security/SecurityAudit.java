package com.taskflow.security;

import org.slf4j.LoggerFactory;

/**
 * Writes bounded security event names and opaque account/resource identifiers through SLF4J.
 * Auth and membership operations call record after the relevant decision or persistence step.
 * Callers must exclude emails, passwords, tokens, and bodies: character sanitization prevents
 * log injection but cannot decide which application data is private. Production Docker logs
 * flow to the configured local or CloudWatch destination.
 */
public final class SecurityAudit {
    private SecurityAudit() { }

    private static String safe(String value) {
        if (value == null) return "-";
        String cleaned = value.replaceAll("[^a-zA-Z0-9_:/.-]", "_");
        return cleaned.substring(0, Math.min(cleaned.length(), 200));
    }

    /**
     * Record event names and opaque actor/resource IDs. Callers must exclude sensitive
     * values; character filtering and length limits prevent log injection, not data disclosure.
     */
    public static void record(String event, String actor, String resource) {
        LoggerFactory.getLogger("com.taskflow.security.audit")
                .info("security_event event={} actor={} resource={}", safe(event), safe(actor), safe(resource));
    }
}
