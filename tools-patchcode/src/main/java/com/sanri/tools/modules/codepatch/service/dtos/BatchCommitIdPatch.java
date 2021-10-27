package com.sanri.tools.modules.codepatch.service.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BatchCommitIdPatch {
    private String group;
    private String repository;
    private List<String> commitIds = new ArrayList<>();
}
