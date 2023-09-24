package org.sqlg.ui.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.umlg.sqlg.structure.topology.*;

public final class VertexLabelUI implements ISqlgTopologyUI {

    private final SchemaUI schemaUI;
    private final SimpleObjectProperty<VertexLabel> vertexLabel;
    private ObservableList<PropertyColumnUI> propertyColumnUIs;
    private ObservableList<EdgeRoleUI> outEdgeRoleUIs;
    private ObservableList<EdgeRoleUI> inEdgeRoleUIs;
    private ObservableList<IndexUI> indexUIs;
    private ObservableList<PartitionUI> partitionUIs;

    private SimpleStringProperty name;
    private SimpleBooleanProperty delete;

    public VertexLabelUI(SchemaUI schemaUI, VertexLabel vertexLabel) {
        this.schemaUI = schemaUI;
        this.vertexLabel = new SimpleObjectProperty<>(vertexLabel);
        init(vertexLabel);
    }

    public VertexLabel getVertexLabel() {
        return vertexLabel.get();
    }

    public SimpleObjectProperty<VertexLabel> vertexLabelProperty() {
        return vertexLabel;
    }

    public void setVertexLabel(VertexLabel vertexLabel) {
        this.name.set(vertexLabel.getName());
        this.vertexLabel.set(vertexLabel);
        this.propertyColumnUIs.clear();
        for (PropertyColumn propertyColumn : vertexLabel.getProperties().values()) {
            this.propertyColumnUIs.add(
                    new PropertyColumnUI(this, null, propertyColumn)
            );
        }
    }

    @Override
    public String getName() {
        return this.name.get();
    }

    @Override
    public StringProperty nameProperty() {
        return this.name;
    }

    public SchemaUI getSchemaUI() {
        return schemaUI;
    }

    public ObservableList<EdgeRoleUI> getOutEdgeRoleUIs() {
        return this.outEdgeRoleUIs;
    }

    public ObservableList<EdgeRoleUI> getInEdgeRoleUIs() {
        return this.inEdgeRoleUIs;
    }

    public ObservableList<PropertyColumnUI> getPropertyColumnUIs() {
        return propertyColumnUIs;
    }

    public ObservableList<IndexUI> getIndexUIs() {
        return indexUIs;
    }

    public ObservableList<PartitionUI> getPartitionUIs() {
        return partitionUIs;
    }

    public boolean isDelete() {
        return delete.get();
    }

    public SimpleBooleanProperty deleteProperty() {
        return delete;
    }

    @Override
    public String getTopologyTypeName() {
        return "VertexLabel";
    }

    @Override
    public void remove() {
        getVertexLabel().remove();
        getSchemaUI().getVertexLabelUIs().remove(this);
    }

    private void init(VertexLabel vertexLabel) {
        this.name = new SimpleStringProperty(vertexLabel.getName());
        this.propertyColumnUIs = FXCollections.observableArrayList();
        for (PropertyColumn propertyColumn : vertexLabel.getProperties().values()) {
            this.propertyColumnUIs.add(new PropertyColumnUI(this, null, propertyColumn));
        }
        this.outEdgeRoleUIs = FXCollections.observableArrayList();
        for (EdgeRole outEdgeRole : vertexLabel.getOutEdgeRoles().values()) {
            this.outEdgeRoleUIs.add(new EdgeRoleUI(this, null, outEdgeRole));
        }
        this.inEdgeRoleUIs = FXCollections.observableArrayList();
        for (EdgeRole inEdgeRole : vertexLabel.getInEdgeRoles().values()) {
            this.inEdgeRoleUIs.add(new EdgeRoleUI(this, null, inEdgeRole));
        }
        this.indexUIs = FXCollections.observableArrayList();
        for (Index index : vertexLabel.getIndexes().values()) {
            this.indexUIs.add(
                    new IndexUI(this, null, index)
            );
        }
        this.partitionUIs = FXCollections.observableArrayList();
        for (Partition partition : vertexLabel.getPartitions().values()) {
            this.partitionUIs.add(
                    new PartitionUI(this, null, partition)
            );
        }
        this.delete = new SimpleBooleanProperty(false);
    }

    public void refresh() {
        init(this.vertexLabel.get());
    }

    public void selectInTree(String name) {
        GraphConfiguration graphConfiguration = this.getSchemaUI().getGraphConfiguration();
        GraphGroup graphGroup = graphConfiguration.getGraphGroup();
        graphConfiguration
                .getLeftPaneController()
                .selectVertexLabel(
                        graphGroup,
                        graphConfiguration,
                        getSchemaUI().getSchema(),
                        name
                );
    }

}
