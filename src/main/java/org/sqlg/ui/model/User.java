package org.sqlg.ui.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.sqlg.ui.controller.GremlinQueryTab;

import java.time.LocalDateTime;

public class User {

    private final Root root;
    private String username;
    private String password;
    private final ListOrderedSet<GraphGroup> graphGroups = new ListOrderedSet<>();
    private final ObservableList<QueryHistoryUI> queryHistoryUIS = FXCollections.observableArrayList();
    ;

    public User(Root root, String username, String password) {
        this.root = root;
        this.username = username;
        this.password = password;
    }

    public Root getRoot() {
        return root;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ListOrderedSet<GraphGroup> getGraphGroups() {
        return graphGroups;
    }

    public ObservableList<QueryHistoryUI> getQueryHistoryUIS() {
        return queryHistoryUIS;
    }

    public ObjectNode toJson(ObjectMapper objectMapper) {
        ObjectNode userObjectNode = objectMapper.createObjectNode();
        userObjectNode.put("username", this.username);
        ArrayNode graphGroupArrayNode = objectMapper.createArrayNode();
        userObjectNode.set("graphGroups", graphGroupArrayNode);
        for (GraphGroup graphGroup : this.graphGroups) {
            graphGroupArrayNode.add(graphGroup.toJson(objectMapper));
        }
        ArrayNode queryHistoryArrayNode = objectMapper.createArrayNode();
        userObjectNode.set("queryHistory", queryHistoryArrayNode);
        int count = 1;
        for (QueryHistoryUI queryHistoryUI : this.queryHistoryUIS) {
            queryHistoryArrayNode.add(queryHistoryUI.getQueryHistory().toJson(objectMapper));
            //only keep a 1000 gremlins
            if (count++ > 1000) {
                break;
            }
        }
        return userObjectNode;
    }

    public void fromJson(ObjectNode userObjectNode) {
        ArrayNode graphGroupArrayNode = (ArrayNode) userObjectNode.get("graphGroups");
        if (graphGroupArrayNode.isEmpty()) {
            GraphGroup graphGroup = new GraphGroup(this, "default");
            this.getGraphGroups().add(graphGroup);
        } else {
            for (JsonNode graphGroupJson : graphGroupArrayNode) {
                String graphGroupName = graphGroupJson.get("name").asText();
                GraphGroup graphGroup = new GraphGroup(this, graphGroupName);
                this.getGraphGroups().add(graphGroup);
                graphGroup.fromJson(graphGroupJson);
            }
        }
        ArrayNode queryHistoryArrayNode = (ArrayNode) userObjectNode.get("queryHistory");
        if (queryHistoryArrayNode != null) {
            for (JsonNode queryHistoryJson : queryHistoryArrayNode) {
                String group = queryHistoryJson.get("group").asText();
                String graph = queryHistoryJson.get("graph").asText();
                String gremlin = queryHistoryJson.get("gremlin").asText();
                LocalDateTime executionDateTime = LocalDateTime.from(
                        GremlinQueryTab.formatter.parse(
                                queryHistoryJson.get("executionDateTime").asText()
                        )
                );
                GremlinHistory gremlinHistory = new GremlinHistory(gremlin, executionDateTime, group, graph);
                getQueryHistoryUIS().add(new QueryHistoryUI(gremlinHistory));
            }
        }
    }

    public void decryptPasswords() {
        for (GraphGroup graphGroup : this.graphGroups) {
            graphGroup.decryptPasswords();
        }
    }
}
