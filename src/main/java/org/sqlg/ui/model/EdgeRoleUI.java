package org.sqlg.ui.model;

import javafx.beans.property.*;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.umlg.sqlg.structure.topology.EdgeRole;

public final class EdgeRoleUI implements ISqlgTopologyUI {

    private final EdgeLabelUI edgeLabelUI;
    private final VertexLabelUI vertexLabelUI;
    private final SimpleObjectProperty<EdgeRole> edgeRole;
    private final SimpleStringProperty name;
    private final SimpleObjectProperty<Direction> direction;
    //multiplicity
    private final SimpleLongProperty lower;
    private final SimpleLongProperty upper;
    private final SimpleBooleanProperty unique;
    private final SimpleBooleanProperty ordered;
    private final SimpleBooleanProperty delete;

    public EdgeRoleUI(VertexLabelUI vertexLabelUI, EdgeLabelUI edgeLabelUI, EdgeRole edgeRole) {
        this.vertexLabelUI = vertexLabelUI;
        this.edgeLabelUI = edgeLabelUI;
        this.edgeRole = new SimpleObjectProperty<>(edgeRole);
        this.direction = new SimpleObjectProperty<>(edgeRole.getDirection());
        this.name = new SimpleStringProperty(edgeRole.getName());

        this.lower = new SimpleLongProperty(edgeRole.getMultiplicity().lower());
        this.upper = new SimpleLongProperty(edgeRole.getMultiplicity().upper());
        this.unique = new SimpleBooleanProperty(edgeRole.getMultiplicity().unique());
        this.ordered = new SimpleBooleanProperty(edgeRole.getMultiplicity().ordered());

        this.delete = new SimpleBooleanProperty(false);
    }

    @Override
    public String getName() {
        return this.name.get();
    }

    @Override
    public StringProperty nameProperty() {
        return this.name;
    }

    public VertexLabelUI getVertexLabelUI() {
        return vertexLabelUI;
    }

    public EdgeRole getEdgeRole() {
        return edgeRole.get();
    }

    public void setEdgeRole(EdgeRole edgeRole) {
        this.name.set(edgeRole.getName());
        this.edgeRole.set(edgeRole);
    }

    public SimpleObjectProperty<Direction> directionProperty() {
        return direction;
    }

    public SimpleLongProperty lowerProperty() {
        return lower;
    }

    public SimpleLongProperty upperProperty() {
        return upper;
    }

    public SimpleBooleanProperty uniqueProperty() {
        return unique;
    }

    public SimpleBooleanProperty orderedProperty() {
        return ordered;
    }

    public boolean isDelete() {
        return delete.get();
    }

    public SimpleBooleanProperty deleteProperty() {
        return delete;
    }
}
