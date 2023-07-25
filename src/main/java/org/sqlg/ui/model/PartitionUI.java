package org.sqlg.ui.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.umlg.sqlg.structure.topology.Partition;

public final class PartitionUI implements ISqlgTopologyUI{

    private final VertexLabelUI vertexLabelUI;
    private final EdgeLabelUI edgeLabelUI;
    private final SimpleObjectProperty<Partition> partition;
    private final SimpleStringProperty name;
    private final SimpleBooleanProperty delete;

    public PartitionUI(VertexLabelUI vertexLabelUI, EdgeLabelUI edgeLabelUI, Partition partition) {
        assert ((vertexLabelUI != null && edgeLabelUI == null) || (vertexLabelUI == null && edgeLabelUI != null)) : "vertexLabelUI or edgeLabelUI must be null";

        this.vertexLabelUI = vertexLabelUI;
        this.edgeLabelUI = edgeLabelUI;
        this.partition = new SimpleObjectProperty<>(partition);
        this.name = new SimpleStringProperty(partition.getName());
        this.delete = new SimpleBooleanProperty(false);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public StringProperty nameProperty() {
        return null;
    }

    public VertexLabelUI getVertexLabelUI() {
        return vertexLabelUI;
    }

    public EdgeLabelUI getEdgeLabelUI() {
        return edgeLabelUI;
    }

    public Partition getPartition() {
        return partition.get();
    }

    public SimpleObjectProperty<Partition> partitionProperty() {
        return partition;
    }

    public boolean isDelete() {
        return delete.get();
    }

    public SimpleBooleanProperty deleteProperty() {
        return delete;
    }
}
