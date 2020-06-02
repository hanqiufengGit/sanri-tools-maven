package com.sanri.app.dtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratedInfo {
   private List<String> mappers;
   private Map<String,String> entityMapper = new HashMap<>();
   private List<String> entitys;

    public GeneratedInfo() {
        this.entitys = new ArrayList<>();
        this.mappers = new ArrayList<>();
    }

    public List<String> getMappers() {
        return mappers;
    }

    public void setMappers(List<String> mappers) {
        this.mappers = mappers;
    }

    public List<String> getEntitys() {
        return entitys;
    }

    public void setEntitys(List<String> entitys) {
        this.entitys = entitys;
    }

    public void addMapper(String mapper){
        this.mappers.add(mapper);
    }
    public void addEntity(String entity){
        this.entitys.add(entity);
    }

    public void addMapper(String entity,String mapper){
        entityMapper.put(entity,mapper);
    }

    public Map<String, String> getEntityMapper() {
        return entityMapper;
    }

    public void setEntityMapper(Map<String, String> entityMapper) {
        this.entityMapper = entityMapper;
    }
}
