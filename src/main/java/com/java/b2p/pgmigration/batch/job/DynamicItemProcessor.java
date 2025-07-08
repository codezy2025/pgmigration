package com.java.b2p.pgmigration.batch.job;

import com.java.b2p.pgmigration.component.TableData;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynamicItemProcessor implements ItemProcessor<TableData, TableData> {

    @Override
    public TableData process(TableData tableData) throws Exception {
        // Here you can add any processing logic if needed
        // For now, we'll just pass the data through
        System.out.println("Table Data in Processor :" + tableData.toString());
        try {
            // Convert all values to String for safe SQL insertion
            List<LinkedHashMap<String, Object>> processedRecords = tableData.getRecords();
            for (Map<String, Object> record : processedRecords) {
                for (Map.Entry<String, Object> entry : record.entrySet()) {
                    if (entry.getValue() != null) {
                        //    entry.setValue(entry.getValue().toString());
                        String value = entry.getValue().toString();
                        try {
                            // Check for boolean
                            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                                entry.setValue(Boolean.parseBoolean(value));
                            }

                            // Check for integer
                            if (value.matches("-?\\d+")) {
                                try {
                                    entry.setValue(Integer.parseInt(value));
                                } catch (NumberFormatException e) {
                                    entry.setValue(Long.parseLong(value));
                                }
                            }

                            // Check for decimal
                            if (value.matches("-?\\d+\\.\\d+")) {
                                entry.setValue(Double.parseDouble(value));
                            }

                            // Check for timestamp (epoch millis or ISO format)
                            if (value.matches("\\d{13}")) { // 13-digit epoch millis
                                entry.setValue(new Timestamp(Long.parseLong(value)));
                            }
                        } catch (Exception e) {
                            entry.setValue(""); // Fallback to string if conversion fails
                        }
                    }
                }
            }
                return tableData;
            }  catch(Exception e){
                return null;
            }
        }
    }

