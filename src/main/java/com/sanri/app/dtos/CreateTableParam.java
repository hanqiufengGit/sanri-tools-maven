package com.sanri.app.dtos;

import java.util.ArrayList;
import java.util.List;

public class CreateTableParam {
    private String connName;
    private String tableName;
    private String comment;
    private String key;

    List<CreateTableColumnParam> columns = new ArrayList<>();

    public String getConnName() {
        return connName;
    }

    public void setConnName(String connName) {
        this.connName = connName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<CreateTableColumnParam> getColumns() {
        return columns;
    }

    public void setColumns(List<CreateTableColumnParam> columns) {
        this.columns = columns;
    }
}
