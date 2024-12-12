package org.sqlg.ui.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import java.util.HashMap;
import java.util.Map;

public class GremlinResultRow {

    private final SimpleStringProperty label = new SimpleStringProperty();
    private final SimpleStringProperty id = new SimpleStringProperty();
    private final Map<String, ObservableValue<Object>> row = new HashMap<>();

    public void setLabel(String label) {
        this.label.set(label);
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public void add(String key, Object value) {
        this.row.put(key, new SimpleObjectProperty<>(value, key, value));
    }

    public String getLabel() {
        return label.get();
    }

    public ObservableValue<Object> labelProperty() {
        return new SimpleObjectProperty<>(label.get(), "label", label.get());
    }

    public String getId() {
        return id.get();
    }

    public ObservableValue<Object> idProperty() {
        return new SimpleObjectProperty<>(id.get(), "id", id.get());
    }

    public ObservableValue<Object> get(String key) {
        return this.row.get(key);
    }
}
