package com.taskflow.security;

import org.slf4j.LoggerFactory;

/** Security events deliberately exclude credentials, tokens, email addresses, and bodies. */
public final class SecurityAudit {
    private SecurityAudit() { }

    private static String safe(String value) {
        if (value == null) return "-";
        String cleaned = value.replaceAll("[^a-zA-Z0-9_:/.-]", "_");
        return cleaned.substring(0, Math.min(cleaned.length(), 200));
    }

    public static void record(String event, String actor, String resource) {
        LoggerFactory.getLogger("com.taskflow.security.audit")
                .info("security_event event={} actor={} resource={}", safe(event), safe(actor), safe(resource));
    }
}
