package com.taskflow.project_service.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the ModelMapper bean used for project creation and response projection.
 * ProjectMapper performs partial updates and membership mapping explicitly, so their ownership/
 * role rules are not inferred by ModelMapper. Authorization always precedes mapping.
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
