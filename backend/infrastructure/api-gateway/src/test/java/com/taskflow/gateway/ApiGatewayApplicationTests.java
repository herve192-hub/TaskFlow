package com.taskflow.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.cors.reactive.CorsWebFilter;

@SpringBootTest
class ApiGatewayApplicationTests {

    @Autowired
    private CorsWebFilter corsWebFilter;

    @Test
    void contextLoads() {
        assertThat(corsWebFilter).isNotNull();
    }

}
