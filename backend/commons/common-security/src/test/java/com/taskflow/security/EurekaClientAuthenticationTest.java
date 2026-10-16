package com.taskflow.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class EurekaClientAuthenticationTest {
    @Test
    void addsCredentialsOnlyToTheDiscoveryServerAndKeepsTheUrlClean() {
        var builder = new EurekaClientAuthentication().discoveryRestClientBuilder(new MockEnvironment()
                .withProperty("spring.cloud.config.username", "test-client")
                .withProperty("spring.cloud.config.password", "isolated-test-password"));
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://discovery-server:8761/eureka/apps"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dGVzdC1jbGllbnQ6aXNvbGF0ZWQtdGVzdC1wYXNzd29yZA=="))
                .andRespond(withSuccess("ready", MediaType.TEXT_PLAIN));
        server.expect(requestTo("http://another-service:8080/health"))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess("ready", MediaType.TEXT_PLAIN));
        var client = builder.build();
        assertEquals("ready", client.get().uri("http://discovery-server:8761/eureka/apps").retrieve().body(String.class));
        assertEquals("ready", client.get().uri("http://another-service:8080/health").retrieve().body(String.class));
        server.verify();
    }
}
