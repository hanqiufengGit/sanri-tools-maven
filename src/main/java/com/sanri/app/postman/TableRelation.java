package com.sanri.app.postman;

import com.sanri.app.jdbc.Column;
import com.sanri.app.jdbc.Table;

public class TableRelation {
    private Column sourceColumn;
    private Column targetColumn;
    private RelationModel relationModel;

    public TableRelation() {
    }

    public TableRelation(Column sourceColumn,  Column targetColumn, RelationModel relationModel) {
        this.sourceColumn = sourceColumn;
        this.targetColumn = targetColumn;
        this.relationModel = relationModel;
    }

    public enum RelationModel{
        ONE_ONE,ONE_MANY,MANY_MANY
    }


    public Column getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(Column sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public Column getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(Column targetColumn) {
        this.targetColumn = targetColumn;
    }

    public RelationModel getRelationModel() {
        return relationModel;
    }

    public void setRelationModel(RelationModel relationModel) {
        this.relationModel = relationModel;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof TableRelation)){
            return false;
        }
        TableRelation tableRelation = (TableRelation) obj;
        return this.mergeProperts().equals(tableRelation.mergeProperts());
    }

    private String mergeProperts() {
        return this.sourceColumn.getTableName()+this.sourceColumn.getColumnName()+
                this.targetColumn.getTableName()+this.targetColumn.getColumnName()
                +this.relationModel;
    }

    @Override
    public int hashCode() {
        return mergeProperts().hashCode();
    }
}
