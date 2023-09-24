package org.sqlg.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.sqlg.ui.model.*;
import org.umlg.sqlg.structure.topology.Index;

public class TopologyTreeItem extends TreeItem<ISqlgTopologyUI> {

    public static final String VERTEX_LABELS = "VertexLabels";
    public static final String EDGE_LABELS = "EdgeLabels";
    public static final String PROPERTY_COLUMNS = "PropertyColumns";
    public static final String INDEXES = "Indexes";
    public static final String PARTITIONS = "Partitions";
    public static final String OUT_EDGE_ROLES = "Out EdgeRoles";
    public static final String IN_EDGE_ROLES = "In EdgeRoles";

    // We cache whether the File is a leaf or not. A File is a leaf if
    // it is not a directory and does not have any files contained within
    // it. We cache this as isLeaf() is called often, and doing the
    // actual check on File is expensive.
    private boolean isLeaf;

    // We do the children and leaf testing only once, and then set these
    // booleans to false so that we do not check again during this
    // run. A more complete implementation may need to handle more
    // dynamic file system situations (such as where a folder has files
    // added after the TreeView is shown). Again, this is left as an
    // exercise for the reader.
    private boolean isFirstTimeChildren = true;

    public TopologyTreeItem(ISqlgTopologyUI value) {
        super(value);
    }

    @Override
    public boolean isLeaf() {
        switch (getValue()) {
            case GraphGroup ignored2 -> {
                return false;
            }
            case MetaTopology ignored1 -> {
                return false;
            }
            case GraphConfiguration ignored -> {
                return false;
            }
            case SchemaUI schemaUI -> {
                return schemaUI.getSchema().getVertexLabels().isEmpty();
            }
            case VertexLabelUI vertexLabelUI -> {
                return false;
            }
            case PropertyColumnUI ignored4 -> {
                return true;
            }
            case EdgeLabelUI edgeLabelUI -> {
                return false;
            }
            case IndexUI ignored5 -> {
                return true;
            }
            case EdgeRoleUI edgeRoleUI -> {
                return true;
            }
            case PartitionUI partitionUI -> {
                return false;
            }
        }
    }

    @Override
    public ObservableList<TreeItem<ISqlgTopologyUI>> getChildren() {
        if (this.isFirstTimeChildren) {
            this.isFirstTimeChildren = false;
            // First getChildren() call, so we actually go off and
            // determine the children of the File contained in this TreeItem.
            super.getChildren().setAll(buildChildren());
        }
        return super.getChildren();
    }

    private ObservableList<TreeItem<ISqlgTopologyUI>> buildChildren() {
        switch (getValue()) {
            case GraphGroup ignored2 -> {
            }
            case MetaTopology ignored1 -> {
            }
            case GraphConfiguration ignored -> {
            }
            case SchemaUI schemaUI -> {
                ObservableList<TreeItem<ISqlgTopologyUI>> metaSchemaTreeItems = FXCollections.observableArrayList();
                TreeItem<ISqlgTopologyUI> metaVertexLabels = new TreeItem<>(new MetaTopology(VERTEX_LABELS, schemaUI));
                metaSchemaTreeItems.add(metaVertexLabels);
                ObservableList<TreeItem<ISqlgTopologyUI>> vertexLabelTreeItems = FXCollections.observableArrayList();
                for (VertexLabelUI vertexLabelUI : schemaUI.getVertexLabelUIs()) {
                    vertexLabelTreeItems.add(new TopologyTreeItem(vertexLabelUI));
                }
                metaVertexLabels.getChildren().addAll(vertexLabelTreeItems);

                TreeItem<ISqlgTopologyUI> metaEdgeLabels = new TreeItem<>(new MetaTopology(EDGE_LABELS, schemaUI));
                metaSchemaTreeItems.add(metaEdgeLabels);
                ObservableList<TreeItem<ISqlgTopologyUI>> edgeLabelTreeItems = FXCollections.observableArrayList();
                for (EdgeLabelUI edgeLabelUI : schemaUI.getEdgeLabelUIS()) {
                    edgeLabelTreeItems.add(new TopologyTreeItem(edgeLabelUI));
                }
                metaEdgeLabels.getChildren().addAll(edgeLabelTreeItems);
                return metaSchemaTreeItems;
            }
            case VertexLabelUI vertexLabelUI -> {
                ObservableList<TreeItem<ISqlgTopologyUI>> metaVertexLabelTreeItems = FXCollections.observableArrayList();
                TreeItem<ISqlgTopologyUI> metaPropertyColumn = new TreeItem<>(new MetaTopology(PROPERTY_COLUMNS, vertexLabelUI));
                metaVertexLabelTreeItems.add(metaPropertyColumn);

                ObservableList<TreeItem<ISqlgTopologyUI>> propertyColumnTreeItems = FXCollections.observableArrayList();
                for (PropertyColumnUI propertyColumnUI : vertexLabelUI.getPropertyColumnUIs()) {
                    propertyColumnTreeItems.add(new TopologyTreeItem(propertyColumnUI));
                }
                metaPropertyColumn.getChildren().addAll(propertyColumnTreeItems);

                TreeItem<ISqlgTopologyUI> metaOutEdgeRoles = new TreeItem<>(new MetaTopology(OUT_EDGE_ROLES, vertexLabelUI));
                metaVertexLabelTreeItems.add(metaOutEdgeRoles);
                ObservableList<TreeItem<ISqlgTopologyUI>> outEdgeRoleTreeItems = FXCollections.observableArrayList();
                for (EdgeRoleUI outEdgeRoleUI : vertexLabelUI.getOutEdgeRoleUIs()) {
                    outEdgeRoleTreeItems.add(new TopologyTreeItem(outEdgeRoleUI));
                }
                metaOutEdgeRoles.getChildren().addAll(outEdgeRoleTreeItems);

                TreeItem<ISqlgTopologyUI> metaInEdgeRoles = new TreeItem<>(new MetaTopology(IN_EDGE_ROLES, vertexLabelUI));
                metaVertexLabelTreeItems.add(metaInEdgeRoles);
                ObservableList<TreeItem<ISqlgTopologyUI>> inEdgeRoleTreeItems = FXCollections.observableArrayList();
                for (EdgeRoleUI inEdgeRoleUI : vertexLabelUI.getInEdgeRoleUIs()) {
                    inEdgeRoleTreeItems.add(new TopologyTreeItem(inEdgeRoleUI));
                }
                metaInEdgeRoles.getChildren().addAll(inEdgeRoleTreeItems);

                TreeItem<ISqlgTopologyUI> metaIndex = new TreeItem<>(new MetaTopology(INDEXES, vertexLabelUI));
                metaVertexLabelTreeItems.add(metaIndex);
                ObservableList<TreeItem<ISqlgTopologyUI>> indexTreeItems = FXCollections.observableArrayList();
                for (IndexUI indexUI : vertexLabelUI.getIndexUIs()) {
                    indexTreeItems.add(new TopologyTreeItem(indexUI));
                }
                metaIndex.getChildren().addAll(indexTreeItems);

                TreeItem<ISqlgTopologyUI> metaPartition = new TreeItem<>(new MetaTopology(PARTITIONS, vertexLabelUI));
                metaVertexLabelTreeItems.add(metaPartition);
                ObservableList<TreeItem<ISqlgTopologyUI>> partitionTreeItems = FXCollections.observableArrayList();
                for (PartitionUI partitionUI: vertexLabelUI.getPartitionUIs()) {
                    partitionTreeItems.add(new TopologyTreeItem(partitionUI));
                }
                metaPartition.getChildren().addAll(partitionTreeItems);
                return metaVertexLabelTreeItems;
            }
            case EdgeLabelUI edgeLabelUI -> {
                ObservableList<TreeItem<ISqlgTopologyUI>> metaEdgeLabelTreeItems = FXCollections.observableArrayList();
                TreeItem<ISqlgTopologyUI> metaPropertyColumn = new TreeItem<>(new MetaTopology(PROPERTY_COLUMNS, edgeLabelUI));
                metaEdgeLabelTreeItems.add(metaPropertyColumn);

                ObservableList<TreeItem<ISqlgTopologyUI>> propertyColumnTreeItems = FXCollections.observableArrayList();
                for (PropertyColumnUI propertyColumnUI : edgeLabelUI.getPropertyColumnUIs()) {
                    propertyColumnTreeItems.add(new TopologyTreeItem(propertyColumnUI));
                }
                metaPropertyColumn.getChildren().addAll(propertyColumnTreeItems);

                TreeItem<ISqlgTopologyUI> metaOutEdgeRoles = new TreeItem<>(new MetaTopology(OUT_EDGE_ROLES, edgeLabelUI));
                metaEdgeLabelTreeItems.add(metaOutEdgeRoles);
                ObservableList<TreeItem<ISqlgTopologyUI>> outEdgeRoleTreeItems = FXCollections.observableArrayList();
                for (EdgeRoleUI outEdgeRoleUI : edgeLabelUI.getOutEdgeRoleUIs()) {
                    outEdgeRoleTreeItems.add(new TopologyTreeItem(outEdgeRoleUI));
                }
                metaOutEdgeRoles.getChildren().addAll(outEdgeRoleTreeItems);

                TreeItem<ISqlgTopologyUI> metaInEdgeRoles = new TreeItem<>(new MetaTopology(IN_EDGE_ROLES, edgeLabelUI));
                metaEdgeLabelTreeItems.add(metaInEdgeRoles);
                ObservableList<TreeItem<ISqlgTopologyUI>> inEdgeRoleTreeItems = FXCollections.observableArrayList();
                for (EdgeRoleUI inEdgeRoleUI : edgeLabelUI.getInEdgeRoleUIs()) {
                    inEdgeRoleTreeItems.add(new TopologyTreeItem(inEdgeRoleUI));
                }
                metaInEdgeRoles.getChildren().addAll(inEdgeRoleTreeItems);

                TreeItem<ISqlgTopologyUI> metaIndex = new TreeItem<>(new MetaTopology(INDEXES, edgeLabelUI));
                metaEdgeLabelTreeItems.add(metaIndex);
                ObservableList<TreeItem<ISqlgTopologyUI>> indexTreeItems = FXCollections.observableArrayList();
                for (Index index : edgeLabelUI.getEdgeLabel().getIndexes().values()) {
                    indexTreeItems.add(new TopologyTreeItem(new IndexUI(null, edgeLabelUI, index)));
                }
                metaIndex.getChildren().addAll(indexTreeItems);

                TreeItem<ISqlgTopologyUI> metaPartition = new TreeItem<>(new MetaTopology(PARTITIONS, edgeLabelUI));
                metaEdgeLabelTreeItems.add(metaPartition);
                ObservableList<TreeItem<ISqlgTopologyUI>> partitionTreeItems = FXCollections.observableArrayList();
                for (PartitionUI partitionUI: edgeLabelUI.getPartitionUIs()) {
                    partitionTreeItems.add(new TopologyTreeItem(partitionUI));
                }
                metaPartition.getChildren().addAll(partitionTreeItems);
                return metaEdgeLabelTreeItems;
            }
            case EdgeRoleUI edgeRoleUI -> {
            }
            case PartitionUI partitionUI -> {

            }
            case PropertyColumnUI propertyColumnUI -> {
            }
            case IndexUI indexUI -> {
            }
        }
        return FXCollections.emptyObservableList();
    }

    void search() {

    }
}
