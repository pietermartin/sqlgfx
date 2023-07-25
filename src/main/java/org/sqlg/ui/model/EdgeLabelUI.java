package org.sqlg.ui.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.umlg.sqlg.structure.topology.EdgeLabel;
import org.umlg.sqlg.structure.topology.Index;
import org.umlg.sqlg.structure.topology.Partition;
import org.umlg.sqlg.structure.topology.PropertyColumn;

public final class EdgeLabelUI implements ISqlgTopologyUI {

    private final SchemaUI schemaUI;
    private final SimpleObjectProperty<EdgeLabel> edgeLabel;
    private final SimpleStringProperty name;
    private final ObservableList<PropertyColumnUI> propertyColumnUIs;
    private final ObservableList<PartitionUI> partitionUIs;
    private final ObservableList<IndexUI> indexUIs;
    private final SimpleBooleanProperty delete;

    public EdgeLabelUI(SchemaUI schemaUI, EdgeLabel edgeLabel) {
        this.schemaUI = schemaUI;
        this.edgeLabel = new SimpleObjectProperty<>(edgeLabel);
        this.name = new SimpleStringProperty(edgeLabel.getName());
        this.propertyColumnUIs = FXCollections.observableArrayList();
        for (PropertyColumn propertyColumn : edgeLabel.getProperties().values()) {
            this.propertyColumnUIs.add(new PropertyColumnUI(null, this, propertyColumn));
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
                    new PartitionUI(null, this, partition)
            );
        }
        this.delete = new SimpleBooleanProperty(false);
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

    @Override
    public void remove() {
        getEdgeLabel().remove();
    }

    public ObservableList<PropertyColumnUI> getPropertyColumnUIs() {
        return this.propertyColumnUIs;
    }
}
