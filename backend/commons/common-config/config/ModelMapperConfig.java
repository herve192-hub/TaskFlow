// // 
// // 
// package com.taskflow.common_config.config;

// import org.modelmapper.Conditions;
// import org.modelmapper.ModelMapper;
// import org.modelmapper.convention.MatchingStrategies;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class ModelMapperConfig {

//     @Bean
//     public ModelMapper modelMapper() {

//         ModelMapper mapper = new ModelMapper();

//         // Enable strict property matching
//         mapper.getConfiguration()

//                 .setMatchingStrategy(MatchingStrategies.STRICT)

//                 // Ignore null values when mapping
//                 .setPropertyCondition(Conditions.isNotNull())

//                 // Skip ambiguous mappings
//                 .setAmbiguityIgnored(true)

//                 // Enable field matching
//                 .setFieldMatchingEnabled(true)

//                 // Allow mapping private fields
//                 .setFieldAccessLevel(
//                         org.modelmapper.config.Configuration.AccessLevel.PRIVATE
//                 );

//         return mapper;
//     }
// }