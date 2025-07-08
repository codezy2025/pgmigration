package com.java.b2p.pgmigration.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendToDlq(String originalTopic, String message, Exception exception) {
        try {
            DlqMessage dlqMessage = new DlqMessage(originalTopic, message, exception.getMessage());
            String dlqTopic = originalTopic + ".dlq";
            kafkaTemplate.send(dlqTopic, dlqMessage.toString());
            log.warn("Sent message to DLQ topic {}: {}", dlqTopic, dlqMessage);
        } catch (Exception e) {
            log.error("Failed to send message to DLQ", e);
        }
    }

    @RequiredArgsConstructor
    private static class DlqMessage {
        private final String originalTopic;
        private final String originalMessage;
        private final String error;

        @Override
        public String toString() {
            return "{" +
                    "\"originalTopic\":\"" + originalTopic + '\"' +
                    ", \"originalMessage\":" + originalMessage +
                    ", \"error\":\"" + error + '\"' +
                    '}';
        }
    }
}