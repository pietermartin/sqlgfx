package org.sqlg.ui.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.set.ListOrderedSet;

public class User {

    private final Root root;
    private String username;
    private String password;
    private final ListOrderedSet<GraphGroup> graphGroups = new ListOrderedSet<>();
    private final ListOrderedSet<QueryHistory> queryHistories = new ListOrderedSet<>();

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

    public ListOrderedSet<QueryHistory> getQueryHistories() {
        return queryHistories;
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
        for (QueryHistory queryHistory : this.queryHistories) {
            queryHistoryArrayNode.add(queryHistory.toJson(objectMapper));
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

            }
        }
    }

    public void decryptPasswords() {
        for (GraphGroup graphGroup : this.graphGroups) {
            graphGroup.decryptPasswords();
        }
    }
}
