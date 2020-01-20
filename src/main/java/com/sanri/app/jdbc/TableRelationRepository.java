package com.sanri.app.jdbc;

import com.sanri.app.postman.TableRelation;

import java.util.*;
import java.util.stream.Collectors;

public class TableRelationRepository {
    // 连接名 = > schema => 表关系列表
    private static Map<String,Map<String, Set<TableRelation>>> tableRelationMap =  new HashMap<>();
    static {
        // 加载本地的数据表关系信息

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
     * 查询单张表的关系列表
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    public List<TableRelation> hierarchy(String connName,String schemaName,String tableName){

        return null;
    }

    public List<TableRelation> subtypes(String connName,String schemaName,String tableName){

        return null;
    }

    public List<TableRelation> childs(String connName,String schemaName,String tableName){
        Map<String, Set<TableRelation>> schemaRelationMap = loadSchemaRelationMap(connName, schemaName);
        Set<TableRelation> relations = schemaRelationMap.get(schemaName);
        return relations.stream().filter(relation -> relation.getSourceTable().equals(tableName)).collect(Collectors.toList());
    }

    public TableRelation parent(){

        return null;
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
