package org.sqlg.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.sqlg.ui.model.*;
import org.umlg.sqlg.structure.topology.Index;

import java.util.Comparator;

import static org.sqlg.ui.Fontawesome.Type.*;

public class TopologyTreeItem extends TreeItem<ISqlgTopologyUI> {

    public static final String SCHEMAS_LABELS = "Schemas";
    public static final String VERTEX_LABELS = "VertexLabels";
    public static final String EDGE_LABELS = "EdgeLabels";
    public static final String PROPERTY_COLUMNS = "PropertyColumns";
    public static final String INDEXES = "Indexes";
    public static final String PARTITIONS = "Partitions";
    public static final String SUB_PARTITIONS = "Sub partitions";
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
        internalSetGraphic(value);
    }

    private void internalSetGraphic(ISqlgTopologyUI value) {
        switch (value) {
            case EdgeLabelUI edgeLabelUI -> {
                setGraphic(graphicForTreeItem(edgeLabelUI));
            }
            case EdgeRoleUI edgeRoleUI -> {
            }
            case GraphConfiguration graphConfiguration -> {
            }
            case GraphGroup graphGroup -> {
            }
            case IndexUI indexUI -> {
                setGraphic(graphicForTreeItem(indexUI));
            }
            case MetaTopology metaTopology -> {
            }
            case PartitionUI partitionUI -> {
            }
            case PropertyColumnUI propertyColumnUI -> {
                setGraphic(graphicForTreeItem(propertyColumnUI));
            }
            case SchemaUI schemaUI -> {
            }
            case VertexLabelUI vertexLabelUI -> {
                setGraphic(graphicForTreeItem(vertexLabelUI));
            }
        }
    }

    public static Node graphicForTreeItem(ISqlgTopologyUI value) {
        switch (value) {
            case EdgeLabelUI edgeLabelUI -> {
                return Fontawesome.CODE_MERGE.label(Solid);
            }
            case EdgeRoleUI edgeRoleUI -> {
            }
            case GraphConfiguration graphConfiguration -> {
                return Fontawesome.DATABASE.label(Solid);
            }
            case GraphGroup graphGroup -> {
                return Fontawesome.BARS.label(Solid);
            }
            case IndexUI indexUI -> {
                return Fontawesome.INDENT.label(Solid);
            }
            case MetaTopology metaTopology -> {
                if (metaTopology.getName().equals("dummy")) {
                    return Fontawesome.PLAY.label(Solid);
                } else  if (metaTopology.getName().equals(SCHEMAS_LABELS)) {
                    return Fontawesome.SERVER.label(Regular);
                } else  if (metaTopology.getName().equals(OUT_EDGE_ROLES)) {
                    return Fontawesome.ARROW_RIGHT.label(Solid);
                } else if (metaTopology.getName().equals(IN_EDGE_ROLES)) {
                    return Fontawesome.ARROW_LEFT.label(Solid);
                } else if (metaTopology.getName().equals(INDEXES)) {
                    return Fontawesome.BLOCK_QUOTE.label(Solid);
                } else if (metaTopology.getName().equals(PROPERTY_COLUMNS)) {
                    return Fontawesome.TABLE_COLUMNS.label(Solid);
                } else if (metaTopology.getName().equals(EDGE_LABELS)) {
                    return Fontawesome.LINES_LEANING.label(Solid);
                } else if (metaTopology.getName().equals(VERTEX_LABELS)) {
                    return Fontawesome.OBJECT_COLUMN.label(Solid);
                } else if (metaTopology.getName().equals(PARTITIONS)) {
                    return Fontawesome.SPLIT.label(Solid);
                } else if (metaTopology.getName().equals(SUB_PARTITIONS)) {
                    return Fontawesome.SPLIT.label(Solid);
                } else {
                    throw new IllegalStateException();
                }
            }
            case PartitionUI partitionUI -> {
                return Fontawesome.FILE_DASHED_LINE.label(Light);
            }
            case PropertyColumnUI propertyColumnUI -> {
                return Fontawesome.CHART_SIMPLE_HORIZONTAL.label(Solid);
            }
            case SchemaUI schemaUI -> {
                return Fontawesome.LIST_UL.label(Regular);
            }
            case VertexLabelUI vertexLabelUI -> {
                return Fontawesome.TABLE.label(Solid);
            }
        }
        return null;
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
                return schemaUI.getVertexLabelUIs().isEmpty();
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
                return partitionUI.getSubPartitionUIs().isEmpty();
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
                MetaTopology metaTopology = new MetaTopology(VERTEX_LABELS, schemaUI);
                TreeItem<ISqlgTopologyUI> metaVertexLabels = new TreeItem<>(metaTopology);
                metaVertexLabels.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaSchemaTreeItems.add(metaVertexLabels);
                ObservableList<TreeItem<ISqlgTopologyUI>> vertexLabelTreeItems = FXCollections.observableArrayList();
                ObservableList<VertexLabelUI> vertexLabelUIS = schemaUI.getVertexLabelUIs();
                vertexLabelUIS.sort(Comparator.comparing(VertexLabelUI::getName));
                for (VertexLabelUI vertexLabelUI : vertexLabelUIS) {
                    TopologyTreeItem vertexLabelTopologyTreeItem = new TopologyTreeItem(vertexLabelUI);
                    vertexLabelTreeItems.add(vertexLabelTopologyTreeItem);
                }
                metaVertexLabels.getChildren().addAll(vertexLabelTreeItems);

                metaTopology = new MetaTopology(EDGE_LABELS, schemaUI);
                TreeItem<ISqlgTopologyUI> metaEdgeLabels = new TreeItem<>(metaTopology);
                metaEdgeLabels.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaSchemaTreeItems.add(metaEdgeLabels);
                ObservableList<TreeItem<ISqlgTopologyUI>> edgeLabelTreeItems = FXCollections.observableArrayList();
                ObservableList<EdgeLabelUI> edgeLabelUIS = schemaUI.getEdgeLabelUIS();
                edgeLabelUIS.sort(Comparator.comparing(EdgeLabelUI::getName));
                for (EdgeLabelUI edgeLabelUI : edgeLabelUIS) {
                    TopologyTreeItem topologyTreeItem = new TopologyTreeItem(edgeLabelUI);
                    edgeLabelTreeItems.add(topologyTreeItem);
                }
                metaEdgeLabels.getChildren().addAll(edgeLabelTreeItems);
                return metaSchemaTreeItems;
            }
            case VertexLabelUI vertexLabelUI -> {
                ObservableList<TreeItem<ISqlgTopologyUI>> metaVertexLabelTreeItems = FXCollections.observableArrayList();
                MetaTopology metaTopology = new MetaTopology(PROPERTY_COLUMNS, vertexLabelUI);
                TreeItem<ISqlgTopologyUI> metaPropertyColumn = new TreeItem<>(metaTopology);
                metaPropertyColumn.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaVertexLabelTreeItems.add(metaPropertyColumn);

                ObservableList<TreeItem<ISqlgTopologyUI>> propertyColumnTreeItems = FXCollections.observableArrayList();
                ObservableList<PropertyColumnUI> propertyColumnUIS = vertexLabelUI.getPropertyColumnUIs();
                propertyColumnUIS.sort(Comparator.comparing(PropertyColumnUI::getName));
                for (PropertyColumnUI propertyColumnUI : propertyColumnUIS) {
                    TopologyTreeItem topologyTreeItem = new TopologyTreeItem(propertyColumnUI);
                    propertyColumnTreeItems.add(topologyTreeItem);
                }
                metaPropertyColumn.getChildren().addAll(propertyColumnTreeItems);

                metaTopology = new MetaTopology(OUT_EDGE_ROLES, vertexLabelUI);
                TreeItem<ISqlgTopologyUI> metaOutEdgeRoles = new TreeItem<>(metaTopology);
                metaOutEdgeRoles.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaVertexLabelTreeItems.add(metaOutEdgeRoles);
                ObservableList<TreeItem<ISqlgTopologyUI>> outEdgeRoleTreeItems = FXCollections.observableArrayList();
                ObservableList<EdgeRoleUI> edgeRoleUIS = vertexLabelUI.getOutEdgeRoleUIs();
                edgeRoleUIS.sort(Comparator.comparing(EdgeRoleUI::getName));
                for (EdgeRoleUI outEdgeRoleUI : edgeRoleUIS) {
                    outEdgeRoleTreeItems.add(new TopologyTreeItem(outEdgeRoleUI));
                }
                metaOutEdgeRoles.getChildren().addAll(outEdgeRoleTreeItems);

                metaTopology = new MetaTopology(IN_EDGE_ROLES, vertexLabelUI);
                TreeItem<ISqlgTopologyUI> metaInEdgeRoles = new TreeItem<>(metaTopology);
                metaInEdgeRoles.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaVertexLabelTreeItems.add(metaInEdgeRoles);
                ObservableList<TreeItem<ISqlgTopologyUI>> inEdgeRoleTreeItems = FXCollections.observableArrayList();
                ObservableList<EdgeRoleUI> inEdgeRoleUIS = vertexLabelUI.getInEdgeRoleUIs();
                inEdgeRoleUIS.sort(Comparator.comparing(EdgeRoleUI::getName));
                for (EdgeRoleUI inEdgeRoleUI : inEdgeRoleUIS) {
                    inEdgeRoleTreeItems.add(new TopologyTreeItem(inEdgeRoleUI));
                }
                metaInEdgeRoles.getChildren().addAll(inEdgeRoleTreeItems);

                metaTopology = new MetaTopology(INDEXES, vertexLabelUI);
                TreeItem<ISqlgTopologyUI> metaIndex = new TreeItem<>(metaTopology);
                metaIndex.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaVertexLabelTreeItems.add(metaIndex);
                ObservableList<TreeItem<ISqlgTopologyUI>> indexTreeItems = FXCollections.observableArrayList();
                for (IndexUI indexUI : vertexLabelUI.getIndexUIs()) {
                    TopologyTreeItem topologyTreeItem = new TopologyTreeItem(indexUI);
                    indexTreeItems.add(topologyTreeItem);
                }
                metaIndex.getChildren().addAll(indexTreeItems);

                TreeItem<ISqlgTopologyUI> metaPartition = new TreeItem<>(new MetaTopology(PARTITIONS, vertexLabelUI));
                metaPartition.setGraphic(Fontawesome.SPLIT.label(Solid));
                metaVertexLabelTreeItems.add(metaPartition);
                ObservableList<TreeItem<ISqlgTopologyUI>> partitionTreeItems = FXCollections.observableArrayList();
                for (PartitionUI partitionUI : vertexLabelUI.getPartitionUIs()) {
                    TopologyTreeItem topologyTreeItem = new TopologyTreeItem(partitionUI);
                    topologyTreeItem.setGraphic(Fontawesome.FILE_DASHED_LINE.label(Light));
                    partitionTreeItems.add(topologyTreeItem);
                }
                metaPartition.getChildren().addAll(partitionTreeItems);
                return metaVertexLabelTreeItems;
            }
            case EdgeLabelUI edgeLabelUI -> {
                ObservableList<TreeItem<ISqlgTopologyUI>> metaEdgeLabelTreeItems = FXCollections.observableArrayList();
                MetaTopology metaTopology = new MetaTopology(PROPERTY_COLUMNS, edgeLabelUI);
                TreeItem<ISqlgTopologyUI> metaPropertyColumn = new TreeItem<>(metaTopology);
                metaPropertyColumn.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaEdgeLabelTreeItems.add(metaPropertyColumn);

                ObservableList<TreeItem<ISqlgTopologyUI>> propertyColumnTreeItems = FXCollections.observableArrayList();
                for (PropertyColumnUI propertyColumnUI : edgeLabelUI.getPropertyColumnUIs()) {
                    TopologyTreeItem topologyTreeItem = new TopologyTreeItem(propertyColumnUI);
                    topologyTreeItem.setGraphic(TopologyTreeItem.graphicForTreeItem(propertyColumnUI));
                    propertyColumnTreeItems.add(topologyTreeItem);
                }
                metaPropertyColumn.getChildren().addAll(propertyColumnTreeItems);

                metaTopology = new MetaTopology(OUT_EDGE_ROLES, edgeLabelUI);
                TreeItem<ISqlgTopologyUI> metaOutEdgeRoles = new TreeItem<>(metaTopology);
                metaOutEdgeRoles.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaEdgeLabelTreeItems.add(metaOutEdgeRoles);
                ObservableList<TreeItem<ISqlgTopologyUI>> outEdgeRoleTreeItems = FXCollections.observableArrayList();
                for (EdgeRoleUI outEdgeRoleUI : edgeLabelUI.getOutEdgeRoleUIs()) {
                    outEdgeRoleTreeItems.add(new TopologyTreeItem(outEdgeRoleUI));
                }
                metaOutEdgeRoles.getChildren().addAll(outEdgeRoleTreeItems);

                metaTopology = new MetaTopology(IN_EDGE_ROLES, edgeLabelUI);
                TreeItem<ISqlgTopologyUI> metaInEdgeRoles = new TreeItem<>(metaTopology);
                metaInEdgeRoles.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaEdgeLabelTreeItems.add(metaInEdgeRoles);
                ObservableList<TreeItem<ISqlgTopologyUI>> inEdgeRoleTreeItems = FXCollections.observableArrayList();
                for (EdgeRoleUI inEdgeRoleUI : edgeLabelUI.getInEdgeRoleUIs()) {
                    inEdgeRoleTreeItems.add(new TopologyTreeItem(inEdgeRoleUI));
                }
                metaInEdgeRoles.getChildren().addAll(inEdgeRoleTreeItems);

                metaTopology = new MetaTopology(INDEXES, edgeLabelUI);
                TreeItem<ISqlgTopologyUI> metaIndex = new TreeItem<>(metaTopology);
                metaIndex.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaEdgeLabelTreeItems.add(metaIndex);
                ObservableList<TreeItem<ISqlgTopologyUI>> indexTreeItems = FXCollections.observableArrayList();
                for (Index index : edgeLabelUI.getEdgeLabel().getIndexes().values()) {
                    IndexUI indexUI = new IndexUI(null, edgeLabelUI, index);
                    TopologyTreeItem topologyTreeItem = new TopologyTreeItem(indexUI);
                    topologyTreeItem.setGraphic(TopologyTreeItem.graphicForTreeItem(indexUI));
                    indexTreeItems.add(topologyTreeItem);
                }
                metaIndex.getChildren().addAll(indexTreeItems);

                metaTopology = new MetaTopology(PARTITIONS, edgeLabelUI);
                TreeItem<ISqlgTopologyUI> metaPartition = new TreeItem<>(metaTopology);
                metaPartition.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaEdgeLabelTreeItems.add(metaPartition);
                ObservableList<TreeItem<ISqlgTopologyUI>> partitionTreeItems = FXCollections.observableArrayList();
                for (PartitionUI partitionUI : edgeLabelUI.getPartitionUIs()) {
                    partitionTreeItems.add(new TopologyTreeItem(partitionUI));
                }
                metaPartition.getChildren().addAll(partitionTreeItems);
                return metaEdgeLabelTreeItems;
            }
            case EdgeRoleUI edgeRoleUI -> {
            }
            case PartitionUI partitionUI -> {
                ObservableList<TreeItem<ISqlgTopologyUI>> metaSubPartitionTreeItems = FXCollections.observableArrayList();
                MetaTopology metaTopology = new MetaTopology(SUB_PARTITIONS, partitionUI);
                TreeItem<ISqlgTopologyUI> metaSubPartition = new TreeItem<>(metaTopology);
                metaSubPartition.setGraphic(TopologyTreeItem.graphicForTreeItem(metaTopology));
                metaSubPartitionTreeItems.add(metaSubPartition);
                ObservableList<TreeItem<ISqlgTopologyUI>> subPartitionsTreeItems = FXCollections.observableArrayList();
                ObservableList<PartitionUI> subPartitionUIS = partitionUI.getSubPartitionUIs();
                subPartitionUIS.sort(Comparator.comparing(PartitionUI::getName));
                for (PartitionUI subPartitionUI : subPartitionUIS) {
                    TopologyTreeItem topologyTreeItem = new TopologyTreeItem(subPartitionUI);
                    topologyTreeItem.setGraphic(TopologyTreeItem.graphicForTreeItem(subPartitionUI));
                    subPartitionsTreeItems.add(topologyTreeItem);
                }
                metaSubPartition.getChildren().addAll(subPartitionsTreeItems);
                return metaSubPartitionTreeItems;
            }
            case PropertyColumnUI propertyColumnUI -> {
            }
            case IndexUI indexUI -> {
            }
        }
        return FXCollections.emptyObservableList();
    }

}
