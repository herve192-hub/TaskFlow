// package com.taskflow.common_config.config;

// import com.fasterxml.jackson.annotation.JsonInclude;
// import com.fasterxml.jackson.databind.DeserializationFeature;
// import com.fasterxml.jackson.databind.SerializationFeature;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.converter.json.Jackson2ObjectMapperBuilderCustomizer;

// @Configuration
// public class JacksonConfig {

//     @Bean
//     public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {

//         return builder -> builder

//                 .modules(new JavaTimeModule())

//                 .serializationInclusion(JsonInclude.Include.NON_NULL)

//                 .featuresToDisable(

//                         SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,

//                         SerializationFeature.FAIL_ON_EMPTY_BEANS,

//                         DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,

//                         DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE
//                 );
//     }
// }