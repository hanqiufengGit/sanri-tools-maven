package com.sanri.app.postman;

public class TableRelation {
    private String sourceTable;
    private String sourceColumn;
    private String targetTable;
    private String targetColumn;
    private RelationModel relationModel;

    public TableRelation() {
    }

    public TableRelation(String sourceTable, String sourceColumn, String targetTable, String targetColumn, RelationModel relationModel) {
        this.sourceTable = sourceTable;
        this.sourceColumn = sourceColumn;
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
        this.relationModel = relationModel;
    }

    public enum RelationModel{
        ONE_ONE,ONE_MANY,MANY_MANY
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    public RelationModel getRelationModel() {
        return relationModel;
    }

    public void setRelationModel(RelationModel relationModel) {
        this.relationModel = relationModel;
    }

    public String mergeProperts(){
        return sourceTable+sourceColumn+targetTable+targetColumn+relationModel;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof TableRelation)){
            return false;
        }
        TableRelation tableRelation = (TableRelation) obj;
        return this.mergeProperts().equals(tableRelation.mergeProperts());
    }

    @Override
    public int hashCode() {
        return mergeProperts().hashCode();
    }
}
