package com.taskflow.user_service.config;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides strict, null-skipping mapping rules consumed by UserMapper.
 * Only matching non-null properties are copied, supporting partial profile updates without
 * clearing omitted fields. Identity/status assignment and avatar/preferences validation stay
 * in UserServiceImpl; ModelMapper does not authorize which profile may be changed.
 */
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
