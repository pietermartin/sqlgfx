package org.sqlg.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public final class GraphGroup implements ISqlgTopologyUI {

    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final ObservableList<GraphConfiguration> graphConfigurations = FXCollections.observableArrayList(new ArrayList<>());

    public GraphGroup(String name) {
        this.name.set(name);
    }

    public void add(GraphConfiguration graphConfiguration) {
        this.graphConfigurations.add(graphConfiguration);
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
}
