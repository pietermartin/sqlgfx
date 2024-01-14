package org.sqlg.ui.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.Partition;

public final class PartitionUI implements ISqlgTopologyUI {

    private final VertexLabelUI vertexLabelUI;
    private final EdgeLabelUI edgeLabelUI;
    private final PartitionUI parentPartitionUI;
    private final SimpleObjectProperty<Partition> partition;
    private final SimpleStringProperty name;
    private final SimpleStringProperty from;
    private final SimpleStringProperty to;
    private final SimpleStringProperty in;
    private final SimpleStringProperty partitionType;
    private final SimpleStringProperty partitionExpression;
    private final SimpleBooleanProperty delete;
    private ObservableList<PartitionUI> subPartitionUIs;

    public PartitionUI(VertexLabelUI vertexLabelUI, Partition partition) {
        this(vertexLabelUI, null, null, partition);
    }

    public PartitionUI(EdgeLabelUI edgeLabelUI, Partition partition) {
        this(null, edgeLabelUI, null, partition);
    }

    public PartitionUI(PartitionUI partitionUI, Partition partition) {
        this(null, null, partitionUI, partition);
    }

    private PartitionUI(VertexLabelUI vertexLabelUI, EdgeLabelUI edgeLabelUI, PartitionUI partitionUI, Partition partition) {
        this.vertexLabelUI = vertexLabelUI;
        this.edgeLabelUI = edgeLabelUI;
        this.parentPartitionUI = partitionUI;
        this.partition = new SimpleObjectProperty<>(partition);
        this.name = new SimpleStringProperty(partition.getName());
        this.from = new SimpleStringProperty(partition.getFrom());
        this.to = new SimpleStringProperty(partition.getTo());
        this.in = new SimpleStringProperty(partition.getIn());
        this.partitionType = new SimpleStringProperty(partition.getPartitionType().name());
        this.partitionExpression = new SimpleStringProperty(partition.getPartitionExpression());
        this.delete = new SimpleBooleanProperty(false);
        this.subPartitionUIs = FXCollections.observableArrayList();
        for (Partition subPartition : partition.getPartitions().values()) {
            this.subPartitionUIs.add(new PartitionUI(this, subPartition));
        }
    }

    public SqlgGraph getSqlgGraph() {
        if (this.vertexLabelUI != null) {
            return this.vertexLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
        } else if (this.edgeLabelUI != null) {
            return this.edgeLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
        } else if (this.parentPartitionUI != null) {
            return this.parentPartitionUI.getSqlgGraph();
        } else {
            throw new IllegalStateException("unhandled");
        }
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public String getQualifiedName() {
        if (this.parentPartitionUI != null) {
            return STR."\{this.parentPartitionUI.getQualifiedName()}.\{name.get()}";
        } else {
            if (getVertexLabelUI()!= null) {
                return STR."\{getVertexLabelUI().getQualifiedName()}.\{name.get()}";
            } else {
                return STR."\{getEdgeLabelUI().getQualifiedName()}.\{name.get()}";
            }
        }
    }

    @Override
    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getFrom() {
        return from.get();
    }

    public SimpleStringProperty fromProperty() {
        return from;
    }

    public void setFrom(String from) {
        this.from.set(from);
    }

    public String getTo() {
        return to.get();
    }

    public SimpleStringProperty toProperty() {
        return to;
    }

    public void setTo(String to) {
        this.to.set(to);
    }

    public String getIn() {
        return in.get();
    }

    public SimpleStringProperty inProperty() {
        return in;
    }

    public void setIn(String in) {
        this.in.set(in);
    }

    public String getPartitionType() {
        return partitionType.get();
    }

    public SimpleStringProperty partitionTypeProperty() {
        return partitionType;
    }

    public void setPartitionType(String partitionType) {
        this.partitionType.set(partitionType);
    }

    public String getPartitionExpression() {
        return partitionExpression.get();
    }

    public SimpleStringProperty partitionExpressionProperty() {
        return partitionExpression;
    }

    public void setPartitionExpression(String partitionExpression) {
        this.partitionExpression.set(partitionExpression);
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

    public ObservableList<PartitionUI> getSubPartitionUIs() {
        return subPartitionUIs;
    }

    public void setSubPartitionUIs(ObservableList<PartitionUI> subPartitionUIs) {
        this.subPartitionUIs = subPartitionUIs;
    }
}
