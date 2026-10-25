package com.taskflow.security;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

/**
 * Supplies prototype RestClient builders for authenticated Eureka registration and heartbeats.
 * Credentials come from Config Server client properties or infrastructure startup secrets.
 * The interceptor sends Basic auth only to the discovery-server hostname, avoiding credential
 * leakage to unrelated downstream requests and keeping secrets out of logged discovery URLs.
 */
@Configuration
public class EurekaClientAuthentication {
    @Bean
    // Each consumer gets its own mutable builder, avoiding shared interceptor configuration.
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    RestClient.Builder discoveryRestClientBuilder(Environment environment) {
        String username = environment.getProperty("spring.cloud.config.username",
                environment.getProperty("spring.security.user.name"));
        String password = environment.getProperty("spring.cloud.config.password",
                environment.getProperty("spring.security.user.password"));
        return RestClient.builder().requestInterceptor((request, body, execution) -> {
            // Never attach infrastructure credentials to requests sent to another host.
            if ("discovery-server".equals(request.getURI().getHost()) && username != null && password != null) {
                request.getHeaders().setBasicAuth(username, password);
            }
            return execution.execute(request, body);
        });
    }
}
