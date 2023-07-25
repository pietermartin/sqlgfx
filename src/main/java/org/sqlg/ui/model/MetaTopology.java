package org.sqlg.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class MetaTopology implements ISqlgTopologyUI {

    private final SimpleStringProperty name;
    private final ISqlgTopologyUI parent;

    public MetaTopology(String name, ISqlgTopologyUI parent) {
        this.name = new SimpleStringProperty(name);
        this.parent = parent;
    }

    @Override
    public String getName() {
        return this.name.get();
    }

    @Override
    public StringProperty nameProperty() {
        return this.name;
    }

    public ISqlgTopologyUI getParent() {
        return parent;
    }
}
