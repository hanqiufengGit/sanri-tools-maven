package com.sanri.app.postman;

import com.sanri.app.jdbc.ExConnection;
import com.sanri.app.jdbc.Table;
import com.sanri.initexec.InitJdbcConnections;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CodeGeneratorConfig {
    private ConnectionConfig connectionConfig;
    private PackageConfig packageConfig;
    private EntityConfig entityConfig;

    public static class ConnectionConfig{
        private String connName;
        private String schemaName;
        private String[] tableNames;

        public ExConnection getConnection() throws SQLException {
            ExConnection exConnection = InitJdbcConnections.CONNECTIONS.get(connName);
            return exConnection;
        }

        public List<Table> tables() throws SQLException {
            List<Table> tables = getConnection().tables(schemaName, true);
            List<Table> filterTables = new ArrayList<>();
            for (Table table : tables) {
                String tableName = table.getTableName();
                if(ArrayUtils.contains(tableNames,tableName)){
                    filterTables.add(table);
                }
            }
            return filterTables;
        }

        public String getConnName() {
            return connName;
        }

        public void setConnName(String connName) {
            this.connName = connName;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String[] getTableNames() {
            return tableNames;
        }

        public void setTableNames(String[] tableNames) {
            this.tableNames = tableNames;
        }
    }

    public static class PackageConfig{
        private String base;

        private String mapper;
        private String service;

        private String entity;
        private String vo;
        private String dto;
        private String param;

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getMapper() {
            return mapper;
        }

        public void setMapper(String mapper) {
            this.mapper = mapper;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getEntity() {
            return entity;
        }

        public void setEntity(String entity) {
            this.entity = entity;
        }

        public String getVo() {
            return vo;
        }

        public void setVo(String vo) {
            this.vo = vo;
        }

        public String getDto() {
            return dto;
        }

        public void setDto(String dto) {
            this.dto = dto;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }
    }

    public static class EntityConfig{
        private String baseEntity;
        private String [] interfaces;
        private String [] excludeColumns;
        private String [] supports;
        public String getBaseEntity() {
            return baseEntity;
        }

        public void setBaseEntity(String baseEntity) {
            this.baseEntity = baseEntity;
        }

        public String[] getInterfaces() {
            return interfaces;
        }

        public void setInterfaces(String[] interfaces) {
            this.interfaces = interfaces;
        }

        public String[] getExcludeColumns() {
            return excludeColumns;
        }

        public void setExcludeColumns(String[] excludeColumns) {
            this.excludeColumns = excludeColumns;
        }

        public String[] getSupports() {
            return supports;
        }

        public void setSupports(String[] supports) {
            this.supports = supports;
        }
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public PackageConfig getPackageConfig() {
        return packageConfig;
    }

    public void setPackageConfig(PackageConfig packageConfig) {
        this.packageConfig = packageConfig;
    }

    public EntityConfig getEntityConfig() {
        return entityConfig;
    }

    public void setEntityConfig(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
    }
}
