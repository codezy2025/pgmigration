package com.java.b2p.pgmigration.kafka.processor;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.java.b2p.pgmigration.component.TableData;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class KafkaTopicListener {

    @Value("${app.kafka.batch-size}")
    private int batchSize;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job kafkaToPostgresJob;
    @Autowired
    private ObjectMapper objectMapper;

    private String currentTableName = null;
    private List<LinkedHashMap<String, Object>> currentRecords = new ArrayList<>();


    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) throws Exception {
        // Check if the message is a table name (first message in a sequence)

        if (message.matches("^[a-zA-Z_]+$") && !message.contains("{")) {
            // If we have pending records, process them first
            if (currentTableName != null && !currentRecords.isEmpty()) {
                processBatch();
            }
            currentTableName = message;
            currentRecords.clear();
        } else {
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            // Parse JSON record
            LinkedHashMap<String, Object> record = objectMapper.readValue(
                    message, new TypeReference<LinkedHashMap<String, Object>>() {});
            currentRecords.add(record);

            // Process batch if size threshold is reached
            if (currentRecords.size() >= batchSize) {
                processBatch();
            }
        }
    }

    private void processBatch() throws Exception {
        if (currentTableName == null || currentRecords.isEmpty()) {
            return;
        }

        TableData tableData = new TableData();
        tableData.setTableName(currentTableName);
        tableData.setRecords(new ArrayList<>(currentRecords));
        String tableDataJson = objectMapper.writeValueAsString(tableData);
        System.out.println("Total Table Record :"+tableDataJson);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("tableName", currentTableName)
                .addString("tableData",tableDataJson)
                .toJobParameters();

        jobLauncher.run(kafkaToPostgresJob, jobParameters);

        currentRecords.clear();
    }
}