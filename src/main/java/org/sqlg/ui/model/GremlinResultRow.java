package org.sqlg.ui.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import java.util.*;

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
        if (value.getClass().isArray()) {
            if (value.getClass().getComponentType() == String.class) {
                String[] values = (String[]) value;
                if (values.length != 0) {
                    List<String> valuesList = Arrays.asList(values);
                    String s = valuesList.stream().reduce((a, b) -> a + "," + b).orElseThrow();
                    this.row.put(key, new SimpleObjectProperty<>(s, key, s));
                } else {
                    this.row.put(key, new SimpleObjectProperty<>("", key, ""));
                }
            } else if (value.getClass().getComponentType() == Short.class) {
                Short[] values = (Short[]) value;
                if (values.length != 0) {
                    List<Short> valuesList = Arrays.asList(values);
                    String s = valuesList.stream().map(Object::toString).reduce((a, b) -> a + "," + b).orElseThrow();
                    this.row.put(key, new SimpleObjectProperty<>(s, key, s));
                } else {
                    this.row.put(key, new SimpleObjectProperty<>("", key, ""));
                }
            } else if (value.getClass().getComponentType() == Integer.class) {
                Integer[] values = (Integer[]) value;
                if (values.length != 0) {
                    List<Integer> valuesList = Arrays.asList(values);
                    String s = valuesList.stream().map(Object::toString).reduce((a, b) -> a + "," + b).orElseThrow();
                    this.row.put(key, new SimpleObjectProperty<>(s, key, s));
                } else {
                    this.row.put(key, new SimpleObjectProperty<>("", key, ""));
                }
            } else if (value.getClass().getComponentType() == Long.class) {
                Long[] values = (Long[]) value;
                if (values.length != 0) {
                    List<Long> valuesList = Arrays.asList(values);
                    String s = valuesList.stream().map(Object::toString).reduce((a, b) -> a + "," + b).orElseThrow();
                    this.row.put(key, new SimpleObjectProperty<>(s, key, s));
                } else {
                    this.row.put(key, new SimpleObjectProperty<>("", key, ""));
                }
            } else if (value.getClass().getComponentType() == Float.class) {
                Float[] values = (Float[]) value;
                if (values.length != 0) {
                    List<Float> valuesList = Arrays.asList(values);
                    String s = valuesList.stream().map(Object::toString).reduce((a, b) -> a + "," + b).orElseThrow();
                    this.row.put(key, new SimpleObjectProperty<>(s, key, s));
                } else {
                    this.row.put(key, new SimpleObjectProperty<>("", key, ""));
                }
            } else if (value.getClass().getComponentType() == Double.class) {
                Double[] values = (Double[]) value;
                if (values.length != 0) {
                    List<Double> valuesList = Arrays.asList(values);
                    String s = valuesList.stream().map(Object::toString).reduce((a, b) -> a + "," + b).orElseThrow();
                    this.row.put(key, new SimpleObjectProperty<>(s, key, s));
                } else {
                    this.row.put(key, new SimpleObjectProperty<>("", key, ""));
                }
            } else {
                this.row.put(key, new SimpleObjectProperty<>(value, key, value));
            }
        } else {
            this.row.put(key, new SimpleObjectProperty<>(value, key, value));
        }
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
