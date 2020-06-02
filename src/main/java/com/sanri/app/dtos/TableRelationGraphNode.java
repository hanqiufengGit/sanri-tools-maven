package com.sanri.app.dtos;

import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * 表关系图
 */
public class TableRelationGraphNode {
    private Table table;
    private List<TableRelation> tableRelations = new ArrayList<>();

}
