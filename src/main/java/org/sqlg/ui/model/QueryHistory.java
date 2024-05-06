package org.sqlg.ui.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;

public class QueryHistory {

    private final String query;
    private final LocalDateTime executionDateTime;
    private final String graphConfiguration;

    public QueryHistory(String query, LocalDateTime executionDateTime, String graphConfiguration) {
        this.query = query;
        this.executionDateTime = executionDateTime;
        this.graphConfiguration = graphConfiguration;
    }

    public ObjectNode toJson(ObjectMapper objectMapper) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("query", getQuery());
        objectNode.put("executionDateTime", getExecutionDateTime().toString());
        objectNode.put("db", getGraphConfiguration());
        return objectNode;
    }

    public String getQuery() {
        return query;
    }

    public LocalDateTime getExecutionDateTime() {
        return executionDateTime;
    }

    public String getGraphConfiguration() {
        return graphConfiguration;
    }
}
