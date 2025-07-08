package com.java.b2p.pgmigration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.b2p.pgmigration.batch.job.DynamicItemProcessor;
import com.java.b2p.pgmigration.batch.job.DynamicJdbcBatchItemWriter;
import com.java.b2p.pgmigration.batch.job.TableDataReader;
import com.java.b2p.pgmigration.component.TableData;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.util.Collections;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
@Autowired
private JobRepository jobRepository;
@Autowired
private ObjectMapper objectMapper;
    @Bean
    public Job kafkaToPostgresJob(Step processTableDataStep) {
        return new JobBuilder("kafkaToPostgresJob",jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(processTableDataStep)
                .end()
                .build();
    }

    @Bean
    public Step processTableDataStep(ItemReader<TableData> tableDataReader,
                                     ItemProcessor<TableData, TableData> tableDataProcessor,
                                     DynamicJdbcBatchItemWriter tableDataWriter,
                                     PlatformTransactionManager transactionManager) {
        return new StepBuilder("processTableDataStep",jobRepository)
                .<TableData, TableData>chunk(10,transactionManager)
                .reader(tableDataReader)
                .processor(tableDataProcessor)
                .writer(tableDataWriter)
                .build();
    }

//    @Bean
//    public ItemReader<TableData> tableDataReader() {
//        // This will be populated by the Kafka listener
//        return new ListItemReader<>(Collections.emptyList());
//    }

    @Bean
    @StepScope
    public TableDataReader tableDataReader(@Value("#{jobParameters['tableData']}") String tableDataJson) throws IOException {
        TableDataReader reader = new TableDataReader();
        //objectMapper=new ObjectMapper();
        TableData tableData = objectMapper.readValue(tableDataJson, TableData.class);
        reader.setTableData(tableData);
        return reader;
    }


    @Bean
    public ItemProcessor<TableData, TableData> tableDataProcessor() {
        return new DynamicItemProcessor();
    }

    @Bean
    public DynamicJdbcBatchItemWriter tableDataWriter() {
        return new DynamicJdbcBatchItemWriter();
    }
}