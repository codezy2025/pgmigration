package com.java.b2p.pgmigration.batch.job;


import com.java.b2p.pgmigration.component.TableData;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynamicJdbcBatchItemWriter implements ItemWriter<TableData> {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends TableData> chunk) throws Exception {
        System.out.println("Table Data in Writer :"+chunk.getItems());
        for (TableData tableData : chunk.getItems()) {
            if (tableData.getRecords() == null || tableData.getRecords().isEmpty()) {
                continue;
            }

            String tableName = tableData.getTableName();
            List<LinkedHashMap<String, Object>> records = tableData.getRecords();

            if (!StringUtils.hasText(tableName)) {
                throw new IllegalArgumentException("Table name cannot be empty");
            }

            // Create SQL dynamically based on the first record's keys
            Map<String, Object> firstRecord = records.get(0);
            String columns = String.join(", ", firstRecord.keySet());
            String placeholders = ":" + String.join(", :", firstRecord.keySet());

            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columns, placeholders);

            // Convert records to SqlParameterSource array
            SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(records.toArray());

            // Execute batch insert
            jdbcTemplate.batchUpdate(sql, batch);
        }
    }
}