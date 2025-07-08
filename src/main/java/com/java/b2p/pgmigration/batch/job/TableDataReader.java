package com.java.b2p.pgmigration.batch.job;

import com.java.b2p.pgmigration.component.TableData;
import org.springframework.batch.item.ItemReader;

public class TableDataReader implements ItemReader<TableData> {

    private TableData tableData;
    private boolean read = false;

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
        this.read = false;
    }

    @Override
    public TableData read() {
        if (tableData == null || read) {
            return null;
        }
        read = true;
        return tableData;
    }
}