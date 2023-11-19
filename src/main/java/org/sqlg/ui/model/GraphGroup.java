package org.sqlg.ui.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Objects;

public final class GraphGroup implements ISqlgTopologyUI {

    private final User user;
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final ObservableList<GraphConfiguration> graphConfigurations = FXCollections.observableArrayList(new ArrayList<>());

    public GraphGroup(User user, String name) {
        this.user = user;
        this.name.set(name);
    }

    public User getUser() {
        return user;
    }

    public void add(GraphConfiguration graphConfiguration) {
        this.graphConfigurations.add(graphConfiguration);
    }

    public void add(String name, String url, String username, boolean savePassword, String password) {
        GraphConfiguration graphConfiguration = new GraphConfiguration(
                this.user.getUsername(),
                this,
                name,
                url,
                username,
                savePassword,
                password,
                GraphConfiguration.TESTED.UNTESTED
        );
        add(graphConfiguration);
        this.user.getRoot().persistConfig();
    }

    public void remove(GraphConfiguration graphConfiguration) {
        this.graphConfigurations.remove(graphConfiguration);
        this.user.getRoot().persistConfig();
    }

    public ObservableList<GraphConfiguration> getGraphConfigurations() {
        return graphConfigurations;
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphGroup that = (GraphGroup) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "GraphGroup{" +
                "name=" + name +
                '}';
    }

    public ObjectNode toJson(ObjectMapper objectMapper) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("name", getName());
        ArrayNode graphConfigurationArrayNode = objectMapper.createArrayNode();
        for (GraphConfiguration graphConfiguration : getGraphConfigurations()) {
            ObjectNode graphConfigurationObjectNode = graphConfiguration.toJson(objectMapper);
            graphConfigurationArrayNode.add(graphConfigurationObjectNode);
        }
        objectNode.set("graphConfigurations", graphConfigurationArrayNode);
        return objectNode;
    }

    public void fromJson(JsonNode graphGroupJson) {
        ArrayNode graphConfigurationArrayNode = (ArrayNode) graphGroupJson.get("graphConfigurations");
        for (JsonNode graphConfigurationJson : graphConfigurationArrayNode) {
            GraphConfiguration graphConfiguration = GraphConfiguration.fromJson(
                    this.getUser(),
                    this,
                    (ObjectNode) graphConfigurationJson
            );
            this.add(graphConfiguration);
        }
    }

    public void decryptPasswords() {
        for (GraphConfiguration graphConfiguration : graphConfigurations) {
            graphConfiguration.decryptPasswords();
        }
    }
}
