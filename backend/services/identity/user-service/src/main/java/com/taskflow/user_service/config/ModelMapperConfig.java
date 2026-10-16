package com.taskflow.user_service.config;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Strict field matching and null-skipping support partial profile updates without erasing omitted data. */
@Configuration
public class ModelMapperConfig {

    @Bean
    ModelMapper modelMapper() {

        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration()

                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setPropertyCondition(Conditions.isNotNull());

        return mapper;
    }

}
