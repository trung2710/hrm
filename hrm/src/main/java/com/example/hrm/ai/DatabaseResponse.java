package com.example.hrm.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DatabaseResponse {
    private boolean success;
    private List<Map<String, Object>> data;
    private String error;
    private QueryType queryType;
    private String originalQuestion;
    private int totalRows;
}