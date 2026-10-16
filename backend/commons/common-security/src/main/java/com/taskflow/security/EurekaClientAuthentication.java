package com.taskflow.security;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

/** Send service credentials as an HTTP header, never embedded in a logged URL. */
@Configuration
public class EurekaClientAuthentication {
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    RestClient.Builder discoveryRestClientBuilder(Environment environment) {
        String username = environment.getProperty("spring.cloud.config.username",
                environment.getProperty("spring.security.user.name"));
        String password = environment.getProperty("spring.cloud.config.password",
                environment.getProperty("spring.security.user.password"));
        return RestClient.builder().requestInterceptor((request, body, execution) -> {
            if ("discovery-server".equals(request.getURI().getHost()) && username != null && password != null) {
                request.getHeaders().setBasicAuth(username, password);
            }
            return execution.execute(request, body);
        });
    }
}
