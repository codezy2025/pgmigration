package com.java.b2p.pgmigration.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.java.b2p.pgmigration.util.DBTypeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JSONConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Java Time module for date/time handling
        mapper.registerModule(new JavaTimeModule());

        // Configure to read string values and convert to appropriate types
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Add custom deserializer for flexible type conversion
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Object.class, new DBTypeSerializer());
        mapper.registerModule(module);

        return mapper;
    }
}