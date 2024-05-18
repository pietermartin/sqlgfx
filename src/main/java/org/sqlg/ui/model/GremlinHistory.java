package org.sqlg.ui.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.sqlg.ui.controller.GremlinQueryTab;

import java.time.LocalDateTime;

public class GremlinHistory {

    private final String gremlin;
    private final LocalDateTime executionDateTime;
    private final String group;
    private final String graph;

    public GremlinHistory(String gremlin, LocalDateTime executionDateTime, String group, String graph) {
        this.gremlin = gremlin;
        this.executionDateTime = executionDateTime;
        this.group = group;
        this.graph = graph;
    }

    public ObjectNode toJson(ObjectMapper objectMapper) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("gremlin", getGremlin());
        objectNode.put("executionDateTime", GremlinQueryTab.formatter.format(getExecutionDateTime()));
        objectNode.put("group", getGroup());
        objectNode.put("graph", getGraph());
        return objectNode;
    }

    public String getGremlin() {
        return gremlin;
    }

    public LocalDateTime getExecutionDateTime() {
        return executionDateTime;
    }

    public String getGroup() {
        return group;
    }

    public String getGraph() {
        return graph;
    }
}
