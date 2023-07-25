package org.sqlg.ui.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.umlg.sqlg.structure.topology.EdgeLabel;
import org.umlg.sqlg.structure.topology.Schema;
import org.umlg.sqlg.structure.topology.VertexLabel;

public final class SchemaUI implements ISqlgTopologyUI {

    private final GraphConfiguration graphConfiguration;
    private final SimpleObjectProperty<Schema> schema;
    private final ObservableList<VertexLabelUI> vertexLabelUIs;
    private final ObservableList<EdgeLabelUI> edgeLabelUIS;
    private final SimpleStringProperty name;
    private final SimpleBooleanProperty delete;

    public SchemaUI(GraphConfiguration graphConfiguration, Schema schema) {
        this.graphConfiguration = graphConfiguration;
        this.name = new SimpleStringProperty(schema.getName());
        this.schema = new SimpleObjectProperty<>(schema);
        this.vertexLabelUIs = FXCollections.observableArrayList();
        for (VertexLabel vertexLabel : schema.getVertexLabels().values()) {
            this.vertexLabelUIs.add(new VertexLabelUI(this, vertexLabel));
        }
        this.edgeLabelUIS = FXCollections.observableArrayList();
        for (EdgeLabel edgeLabel : schema.getEdgeLabels().values()) {
            this.edgeLabelUIS.add(new EdgeLabelUI(this, edgeLabel));
        }
        this.delete = new SimpleBooleanProperty(false);
    }

    public GraphConfiguration getGraphConfiguration() {
        return graphConfiguration;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public Schema getSchema() {
        return schema.get();
    }

    public SimpleObjectProperty<Schema> schemaProperty() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema.set(schema);
    }

    public ObservableList<VertexLabelUI> getVertexLabelUIs() {
        return vertexLabelUIs;
    }

    public ObservableList<EdgeLabelUI> getEdgeLabelUIS() {
        return edgeLabelUIS;
    }

    public boolean isDelete() {
        return delete.get();
    }

    public SimpleBooleanProperty deleteProperty() {
        return delete;
    }

    @Override
    public String getTopologyTypeName() {
        return "Schema";
    }

    @Override
    public void remove() {
        getSchema().remove();
    }
}
