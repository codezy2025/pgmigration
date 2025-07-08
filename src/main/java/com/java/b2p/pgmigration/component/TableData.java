package com.java.b2p.pgmigration.component;


import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

@Data
public class TableData  implements Serializable {
    private String tableName;
    private List<LinkedHashMap<String, Object>> records;
}