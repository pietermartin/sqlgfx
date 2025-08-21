package org.sqlg.ui.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.umlg.sqlg.structure.topology.*;

public final class EdgeLabelUI implements ISqlgTopologyUI {

    private final SchemaUI schemaUI;
    private final SimpleObjectProperty<EdgeLabel> edgeLabel;
    private SimpleStringProperty name;
    private ObservableList<PropertyColumnUI> propertyColumnUIs;
    private ObservableList<EdgeRoleUI> outEdgeRoleUIs;
    private ObservableList<EdgeRoleUI> inEdgeRoleUIs;
    private ObservableList<PartitionUI> partitionUIs;
    private ObservableList<IndexUI> indexUIs;
    private SimpleBooleanProperty delete;

    public EdgeLabelUI(SchemaUI schemaUI, EdgeLabel edgeLabel) {
        this.schemaUI = schemaUI;
        this.edgeLabel = new SimpleObjectProperty<>(edgeLabel);
        init(edgeLabel);
    }

    public void refresh() {
        init(this.edgeLabel.get());
    }

    public void reset() {
        init(this.edgeLabel.get());
    }

    private void init(EdgeLabel edgeLabel) {
        if (this.name == null) {
            this.name = new SimpleStringProperty(edgeLabel.getName());
            this.propertyColumnUIs = FXCollections.observableArrayList();
            for (PropertyColumn propertyColumn : edgeLabel.getProperties().values()) {
                this.propertyColumnUIs.add(new PropertyColumnUI(null, this, propertyColumn));
            }
            this.outEdgeRoleUIs = FXCollections.observableArrayList();
            for (EdgeRole outEdgeRole : edgeLabel.getOutEdgeRoles()) {
                this.outEdgeRoleUIs.add(new EdgeRoleUI(null, this, outEdgeRole));
            }
            this.inEdgeRoleUIs = FXCollections.observableArrayList();
            for (EdgeRole inEdgeRole : edgeLabel.getInEdgeRoles()) {
                this.inEdgeRoleUIs.add(new EdgeRoleUI(null, this, inEdgeRole));
            }
            this.indexUIs = FXCollections.observableArrayList();
            for (Index index : edgeLabel.getIndexes().values()) {
                this.indexUIs.add(
                        new IndexUI(null, this, index)
                );
            }
            this.partitionUIs = FXCollections.observableArrayList();
            for (Partition partition : edgeLabel.getPartitions().values()) {
                this.partitionUIs.add(
                        new PartitionUI(this, partition)
                );
            }
            this.delete = new SimpleBooleanProperty(false);
        } else {
            this.name.set(edgeLabel.getName());
        }
    }

    public EdgeLabel getEdgeLabel() {
        return edgeLabel.get();
    }

    public SimpleObjectProperty<EdgeLabel> edgeLabelProperty() {
        return edgeLabel;
    }

    public void setEdgeLabel(EdgeLabel edgeLabel) {
        this.name.set(edgeLabel.getName());
        this.edgeLabel.set(edgeLabel);
        this.propertyColumnUIs.clear();
        for (PropertyColumn propertyColumn : edgeLabel.getProperties().values()) {
            this.propertyColumnUIs.add(new PropertyColumnUI(null, this, propertyColumn));
        }
    }

    public SchemaUI getSchemaUI() {
        return schemaUI;
    }

    @Override
    public String getName() {
        return this.name.get();
    }

    @Override
    public String getQualifiedName() {
        return getSchemaUI().getGraphConfiguration().getName() + "." + getEdgeLabel().getFullName();
    }

    @Override
    public StringProperty nameProperty() {
        return this.name;
    }

    @Override
    public String getTopologyTypeName() {
        return "EdgeLabel";
    }

    public boolean isDelete() {
        return delete.get();
    }

    public SimpleBooleanProperty deleteProperty() {
        return delete;
    }

    public ObservableList<IndexUI> getIndexUIs() {
        return indexUIs;
    }

    public ObservableList<PartitionUI> getPartitionUIs() {
        return partitionUIs;
    }

    public ObservableList<EdgeRoleUI> getOutEdgeRoleUIs() {
        return outEdgeRoleUIs;
    }

    public void setOutEdgeRoleUIs(ObservableList<EdgeRoleUI> outEdgeRoleUIs) {
        this.outEdgeRoleUIs = outEdgeRoleUIs;
    }

    public ObservableList<EdgeRoleUI> getInEdgeRoleUIs() {
        return inEdgeRoleUIs;
    }

    public void setInEdgeRoleUIs(ObservableList<EdgeRoleUI> inEdgeRoleUIs) {
        this.inEdgeRoleUIs = inEdgeRoleUIs;
    }

    @Override
    public void remove() {
        getEdgeLabel().remove();
    }

    public ObservableList<PropertyColumnUI> getPropertyColumnUIs() {
        return this.propertyColumnUIs;
    }

}
