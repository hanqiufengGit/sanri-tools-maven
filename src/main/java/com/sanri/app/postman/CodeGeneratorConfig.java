package com.sanri.app.postman;

import com.sanri.app.jdbc.ExConnection;
import com.sanri.app.jdbc.Table;
import com.sanri.initexec.InitJdbcConnections;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基本代码生成
 * {
 *     "codeGeneratorConfig": {
 *         "filePath": "d:/test",
 *         "baseMapper": "tk.mybatis.mapper.common.Mapper",
 *         "connectionConfig": {
 *             "connName": "mysql",
 *             "schemaName": "card",
 *             "tableNames": [
 *                 "enterprise_card_base",
 *                 "enterprise_card_core"
 *             ]
 *         },
 *         "packageConfig": {
 *             "base": "com.sanri.card",
 *             "mapper": "com.sanri.card.dao.mapper",
 *             "service": "com.sanri.card.service",
 *             "entity": "com.sanri.card.dao.entity",
 *             "vo": "com.sanri.card.web.vo",
 *             "dto": "com.sanri.card.web.dto",
 *             "param": "com.sanri.card.web.param"
 *         },
 *         "entityConfig": {
 *             "baseEntity": "com.sanri.card.dao.BaseEntity",
 *             "interfaces": [
 *                 "java.io.Serializable"
 *             ],
 *             "excludeColumns": [
 *                 "id"
 *             ],
 *             "supports": [
 *                 "swagger",
 *                 "lombok"
 *             ],
 *             "idColumn":"id",
 *             "sqlStatement":"JDBC"
 *         }
 *     }
 * }
 *
 * 项目代码生成示例
 */
public class CodeGeneratorConfig {
    private ConnectionConfig connectionConfig;
    private PackageConfig packageConfig;
    private EntityConfig entityConfig;
    private MavenConfig mavenConfig;

    private String baseMapper;
    private String filePath;
    private String projectName;

    public static class MavenConfig{
        private String groupId;
        private String artifactId;
        private String version = "1.0-SNAPSHOT";
        private String springBootVersion = "2.0.5.RELEASE";

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSpringBootVersion() {
            return springBootVersion;
        }

        public void setSpringBootVersion(String springBootVersion) {
            this.springBootVersion = springBootVersion;
        }
    }

    public static class ConnectionConfig{
        private String connName;
        private String schemaName;
        private String [] tableNames;

        private String driverClass;
        private String connectionURL;
        private String userId;
        private String password;

        public void config(){
            ExConnection exConnection = InitJdbcConnections.CONNECTIONS.get(connName);
            this.driverClass = exConnection.getDriver();
            this.connectionURL = exConnection.getConnectionURL(schemaName);
            this.userId = exConnection.getUsername();
            this.password = exConnection.getPassword();
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

        public String getDriverClass() {
            return driverClass;
        }

        public void setDriverClass(String driverClass) {
            this.driverClass = driverClass;
        }

        public String getConnectionURL() {
            return connectionURL;
        }

        public void setConnectionURL(String connectionURL) {
            this.connectionURL = connectionURL;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class PackageConfig{
        private String base;

        private String mapper;
        private String service;
        private String controller;

        private String entity;
        private String vo;
        private String dto;
        private String param;

        public String getController() {
            return controller;
        }

        public void setController(String controller) {
            this.controller = controller;
        }

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
        private String idColumn;
        private String sqlStatement;

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

        public String getIdColumn() {
            return idColumn;
        }

        public void setIdColumn(String idColumn) {
            this.idColumn = idColumn;
        }

        public String getSqlStatement() {
            return sqlStatement;
        }

        public void setSqlStatement(String sqlStatement) {
            this.sqlStatement = sqlStatement;
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

    public String getBaseMapper() {
        return baseMapper;
    }

    public void setBaseMapper(String baseMapper) {
        this.baseMapper = baseMapper;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public MavenConfig getMavenConfig() {
        return mavenConfig;
    }

    public void setMavenConfig(MavenConfig mavenConfig) {
        this.mavenConfig = mavenConfig;
    }
}
