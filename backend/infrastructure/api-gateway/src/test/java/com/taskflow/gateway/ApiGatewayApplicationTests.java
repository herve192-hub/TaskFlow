package com.taskflow.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.reactive.CorsWebFilter;
import reactor.core.publisher.Mono;

/**
 * Gateway context/CORS integration tests with controlled application properties.
 * Verify that the configured browser origin can send the expected request headers and an unrelated
 * origin is rejected. Failsafe runs this context suite; the full-stack scenario covers actual routing.
 */
@SpringBootTest(properties = "TASKFLOW_WEB_ORIGIN=http://127.0.0.1:15173")
class ApiGatewayApplicationTests {

    @Autowired
    private CorsWebFilter corsWebFilter;

    @Test
    void contextLoads() {
        assertThat(corsWebFilter).isNotNull();
    }

    @Test
    void configuredFrontendOriginCanSubmitRequests() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.options("http://localhost:8080/auth/register")
                        .header(HttpHeaders.ORIGIN, "http://127.0.0.1:15173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"));

        corsWebFilter.filter(exchange, ignored -> Mono.empty()).block(Duration.ofSeconds(5));

        assertThat(exchange.getResponse().getHeaders().getAccessControlAllowOrigin())
                .isEqualTo("http://127.0.0.1:15173");
        assertThat(exchange.getResponse().getHeaders().getAccessControlAllowMethods())
                .contains(HttpMethod.POST);
    }

    @Test
    void unrelatedOriginIsRejected() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.options("http://localhost:8080/auth/register")
                        .header(HttpHeaders.ORIGIN, "https://untrusted.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"));

        corsWebFilter.filter(exchange, ignored -> Mono.empty()).block(Duration.ofSeconds(5));

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getHeaders().getAccessControlAllowOrigin()).isNull();
    }

}
