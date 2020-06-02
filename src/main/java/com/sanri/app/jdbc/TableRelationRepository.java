package com.sanri.app.jdbc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.sanri.app.BaseServlet;
import com.sanri.app.dtos.TableRelation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表关系管理
 */
public class TableRelationRepository {
    private static TableRelationRepository tableRelationRepository = new TableRelationRepository();

    // 连接名 = > schema => 表关系列表
    private static Map<String,Map<String, Set<TableRelation>>> tableRelationMap =  new HashMap<>();
    private static File relationDirs;
    static {
        // 加载本地的数据表关系信息
        relationDirs = BaseServlet.mkTmpPath("/relations");
        File relations = new File(relationDirs, "relations");
        if(relations.exists()) {
            try {
                String readFileToString = FileUtils.readFileToString(relations);
                // 简单加载,不获取数据表的完整信息
                TypeReference<Map<String, Map<String, Set<TableRelation>>>> mapTypeReference = new TypeReference<Map<String, Map<String, Set<TableRelation>>>>() {
                };
                tableRelationMap = JSON.parseObject(readFileToString, mapTypeReference);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static TableRelationRepository getInstance(){
        return tableRelationRepository;
    }

    /**
     * 新增关系
     * @param connName
     * @param schemaName
     * @param tableRelations
     */
    public void insert(String connName,String schemaName,Set<TableRelation> tableRelations){
        Map<String, Set<TableRelation>> schemaRelationMap = loadSchemaRelationMap(connName, schemaName);
        Set<TableRelation> relations = schemaRelationMap.get(schemaName);
        relations.addAll(tableRelations);
    }

    /**
     * 删除关系
     * @param connName
     * @param schemaName
     * @param tableRelations
     */
    public void drop(String connName,String schemaName,Set<TableRelation> tableRelations){
        Map<String, Set<TableRelation>> schemaRelationMap = loadSchemaRelationMap(connName, schemaName);
        Set<TableRelation> relations = schemaRelationMap.get(schemaName);
        relations.removeAll(tableRelations);
    }

    /**
     * 查询某个表使用其它表的表关系
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    public List<TableRelation> childs(String connName,String schemaName,String tableName){
        Map<String, Set<TableRelation>> schemaRelationMap = loadSchemaRelationMap(connName, schemaName);
        Set<TableRelation> relations = schemaRelationMap.get(schemaName);
        return relations.stream().filter(relation -> relation.getSourceColumn().getTableName().equals(tableName)).collect(Collectors.toList());
    }

    /**
     * 查询使用某个表的表关系
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    public List<TableRelation> parents(String connName,String schemaName,String tableName){
        Map<String, Set<TableRelation>> schemaRelationMap = loadSchemaRelationMap(connName, schemaName);
        Set<TableRelation> relations = schemaRelationMap.get(schemaName);
        return relations.stream().filter(relation -> relation.getTargetColumn().getTableName().equals(tableName)).collect(Collectors.toList());
    }

    /**
     * 查询某一张表的表关系
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    public Set<TableRelation> tableRelations(String connName, String schemaName, String tableName) {
        Map<String, Set<TableRelation>> schemaRelationMap = loadSchemaRelationMap(connName, schemaName);
        Set<TableRelation> relations = schemaRelationMap.get(schemaName);
        return relations;
    }

    static class ColumnPropertyFilter implements PropertyFilter{
        private String [] filterColumns = {"columnType","comments","primaryKey"};
        @Override
        public boolean apply(Object object, String name, Object value) {
            if(object != null && object instanceof Column){
                return !ArrayUtils.contains(filterColumns,name);
            }
            return true;
        }
    }
    private final static ColumnPropertyFilter columnPropertyFilter =  new ColumnPropertyFilter();

    /**
     * 序列化现在的所有表关系
     */
    public void serializerRelation(){
        String relationJson = JSON.toJSONString(tableRelationMap);
        try {
            FileUtils.writeStringToFile(new File(relationDirs,"relations"),relationJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Set<TableRelation>> loadSchemaRelationMap(String connName, String schemaName) {
        Map<String, Set<TableRelation>> schemaRelationMap = tableRelationMap.get(connName);
        if(schemaRelationMap == null){
            schemaRelationMap = new HashMap<>();
            schemaRelationMap.put(schemaName,new HashSet<>());
            tableRelationMap.put(connName,schemaRelationMap);
        }else{
            Set<TableRelation> relations = schemaRelationMap.get(schemaName);
            if(relations == null){
                schemaRelationMap.put(schemaName,new HashSet<>());
            }
        }
        return schemaRelationMap;
    }


}
