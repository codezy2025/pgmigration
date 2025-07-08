package com.java.b2p.pgmigration.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.sql.Timestamp;

public class DBTypeSerializer extends JsonDeserializer<Object> {

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            String value = p.getValueAsString();

            if (value == null || value.equalsIgnoreCase("null")) {
                return null;
            }

            // Try to detect and convert to appropriate type
            try {
                // Check for boolean
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    return Boolean.parseBoolean(value);
                }

                // Check for integer
                if (value.matches("-?\\d+")) {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return Long.parseLong(value);
                    }
                }

                // Check for decimal
                if (value.matches("-?\\d+\\.\\d+")) {
                    return Double.parseDouble(value);
                }

                // Check for timestamp (epoch millis or ISO format)
                if (value.matches("\\d{13}")) { // 13-digit epoch millis
                    return new Timestamp(Long.parseLong(value));
                }

                // Default to string
                return value;
            } catch (Exception e) {
                return value; // Fallback to string if conversion fails
            }
        }
    }
