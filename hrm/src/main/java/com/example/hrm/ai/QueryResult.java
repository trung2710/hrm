package com.example.hrm.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QueryResult {
    private QueryType queryType;
    private String sqlQuery;
    private List<Object> parameters;
    private String originalQuestion;
    private String currentUser;
    private UserRole userRole;
    private String explanation;
    private boolean needsUserContext;
}