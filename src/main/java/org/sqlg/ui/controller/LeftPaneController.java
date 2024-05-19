package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.Fontawesome;
import org.sqlg.ui.GraphConfigurationTreeItem;
import org.sqlg.ui.GraphGroupTreeItem;
import org.sqlg.ui.TopologyTreeItem;
import org.sqlg.ui.model.*;
import org.umlg.sqlg.structure.topology.*;

import java.util.*;

import static org.sqlg.ui.Fontawesome.Type.Solid;

public class LeftPaneController {

    private final static Logger LOGGER = LoggerFactory.getLogger(LeftPaneController.class);
    private final PrimaryController primaryController;

    private AutoCompletionBinding<String> autoCompletionBinding;
    private final Set<String> possibleSuggestions = new HashSet<>();
    private TextField searchTextField;

    private TreeView<ISqlgTopologyUI> topologyTreeView;
    private TreeItem<ISqlgTopologyUI> selectedTreeItem;
    private final ObservableList<GraphGroup> graphGroups;
    private final BorderPane leftBorderPane;
    private final BreadCrumbBar<ISqlgTopologyUI> breadCrumbBar;
    private final Tab viewTab;
    private final PgDumpTab pgDumpTab;

    private final int GRAPH_GROUP_INDEX = 1;
    private final int GRAPH_CONFIGURATION_INDEX = 2;
    private final int META_SCHEMA_INDEX = 3;
    private final int SCHEMA_INDEX = 4;
    private final int META_VERTEX_LABEL_INDEX = 5;
    private final int VERTEX_LABEL_OR_EDGE_LABEL_INDEX = 6;
    private final int META_PROPERTY_COLUMN_OR_EDGE_LABEL_OR_INDEX_OR_PARTITIONS = 7;
    private final int PROPERTY_COLUMN_OR_EDGE_LABEL_INDEX = 8;
    private final int META_PROPERTY_COLUMN_INDEX = 9;
    private final int PROPERTY_COLUMN_INDEX_PARTITION = 10;

    public LeftPaneController(
            PrimaryController primaryController,
            BreadCrumbBar<ISqlgTopologyUI> breadCrumbBar,
            ObservableList<GraphGroup> graphGroups,
            BorderPane leftBorderPane,
            BorderPane viewBorderPane) {

        this.primaryController = primaryController;
        this.breadCrumbBar = breadCrumbBar;
        this.graphGroups = graphGroups;
        this.leftBorderPane = leftBorderPane;

        TabPane tabPane = new TabPane();
        this.viewTab = new Tab("Schema", null);
        this.viewTab.setGraphic(Fontawesome.WRENCH.label(Fontawesome.Type.Solid));
        this.viewTab.setClosable(false);

        Tab definitionTab = new Tab("Definition", null);
        definitionTab.setGraphic(Fontawesome.GEARS.label(Fontawesome.Type.Solid));
        definitionTab.setClosable(false);
        this.pgDumpTab = new PgDumpTab(this.primaryController.getStage());
        definitionTab.setContent(pgDumpTab.getView());

        tabPane.getTabs().addAll(this.viewTab, definitionTab);
        viewBorderPane.setCenter(tabPane);
    }

    private static ListChangeListener<GraphConfiguration> graphConfigurationListChangeListener(
            LeftPaneController leftPaneController,
            TreeItem<ISqlgTopologyUI> graphGroupTreeItem) {
        return (c) -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    //noinspection StatementWithEmptyBody
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        //permutate
                    }
                } else //noinspection StatementWithEmptyBody
                    if (c.wasUpdated()) {
                        //update item
                    } else {
                        for (GraphConfiguration remitem : c.getRemoved()) {
                            Set<TreeItem<ISqlgTopologyUI>> toRemove = new HashSet<>();
                            graphGroupTreeItem.getChildren().forEach(treeItem -> {
                                if (treeItem.getValue().getName().equals(remitem.getName())) {
                                    toRemove.add(treeItem);
                                }
                            });
                            graphGroupTreeItem.getChildren().removeAll(toRemove);
                        }
                        for (GraphConfiguration addItem : c.getAddedSubList()) {
                            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = new GraphConfigurationTreeItem(leftPaneController, addItem);
                            graphGroupTreeItem.getChildren().add(graphConfigurationTreeItem);
                        }
                    }
            }
        };
    }

    private void internalPartitionSuggestion(String parent, Partition partition) {
        String _parent = STR."\{parent}.\{partition.getName()}";
        this.possibleSuggestions.add(_parent);
        for (Partition subPartition : partition.getPartitions().values()) {
            internalPartitionSuggestion(_parent, subPartition);
        }
    }

    public void addToSuggestions(GraphConfiguration graphConfiguration) {
        Topology topology = graphConfiguration.getSqlgGraph().getTopology();
        String graphConfigurationName = graphConfiguration.getName();
        for (Schema schema : topology.getSchemas()) {
            this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{schema.getName()}");
            for (VertexLabel vertexLabel : schema.getVertexLabels().values()) {
                this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{vertexLabel.getFullName()}");
                for (PropertyColumn propertyColumn : vertexLabel.getProperties().values()) {
                    this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{vertexLabel.getFullName()}.\{propertyColumn.getName()}");
                }
                for (EdgeRole outEdgeRole : vertexLabel.getOutEdgeRoles().values()) {
                    this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{vertexLabel.getFullName()}.\{outEdgeRole.getName()}");
                }
                for (EdgeRole inEdgeRole : vertexLabel.getInEdgeRoles().values()) {
                    this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{vertexLabel.getFullName()}.\{inEdgeRole.getName()}");
                }
                for (Index index : vertexLabel.getIndexes().values()) {
                    this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{vertexLabel.getFullName()}.\{index.getName()}");
                }
                for (Partition partition : vertexLabel.getPartitions().values()) {
                    internalPartitionSuggestion(STR."\{graphConfigurationName}.\{vertexLabel.getFullName()}", partition);
                }
            }
            for (EdgeLabel edgeLabel : schema.getEdgeLabels().values()) {
                this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{edgeLabel.getFullName()}");
                for (PropertyColumn propertyColumn : edgeLabel.getProperties().values()) {
                    this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{edgeLabel.getFullName()}.\{propertyColumn.getName()}");
                }
                for (EdgeRole outEdgeRole : edgeLabel.getOutEdgeRoles()) {
                    this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{edgeLabel.getFullName()}.\{outEdgeRole.getName()}");
                }
                for (EdgeRole inEdgeRole : edgeLabel.getInEdgeRoles()) {
                    this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{edgeLabel.getFullName()}.\{inEdgeRole.getName()}");
                }
                for (Index index : edgeLabel.getIndexes().values()) {
                    this.possibleSuggestions.add(STR."\{graphConfigurationName}.\{edgeLabel.getFullName()}.\{index.getName()}");
                }
                for (Partition partition : edgeLabel.getPartitions().values()) {
                    internalPartitionSuggestion(STR."\{graphConfigurationName}.\{edgeLabel.getFullName()}", partition);
                }
            }
        }
        // we dispose the old binding and recreate a new binding
        if (this.autoCompletionBinding != null) {
            this.autoCompletionBinding.dispose();
        }
        this.autoCompletionBinding = TextFields.bindAutoCompletion(this.searchTextField, this.possibleSuggestions);
        this.autoCompletionBinding.setPrefWidth(this.searchTextField.getWidth());
    }

    protected void initialize() {

        this.searchTextField = TextFields.createClearableTextField();
        this.searchTextField.setPromptText("search");
        TextFields.bindAutoCompletion(
                this.searchTextField,
                ignore -> List.of());
        this.searchTextField.setOnKeyPressed(
                ke -> {
                    if (Objects.requireNonNull(ke.getCode()) == KeyCode.ENTER) {
                        TreeItem<ISqlgTopologyUI> topologyTreeItem = search(
                                this.topologyTreeView.getRoot(),
                                this.searchTextField.getText()
                        );
                        if (topologyTreeItem != null) {
                            this.topologyTreeView.getSelectionModel().select(topologyTreeItem);
                            this.topologyTreeView.scrollTo(this.topologyTreeView.getSelectionModel().getSelectedIndex());
                        }
                    }
                }
        );

        HBox searchHBox = new HBox(5);
        searchHBox.setPadding(new Insets(5, 5, 5, 5));
        searchHBox.getChildren().add(this.searchTextField);
        HBox.setHgrow(this.searchTextField, Priority.ALWAYS);
        this.leftBorderPane.setTop(searchHBox);

        this.topologyTreeView = new TreeView<>();
        this.leftBorderPane.setCenter(this.topologyTreeView);

        this.topologyTreeView.setEditable(false);
        TreeItem<ISqlgTopologyUI> dummyRoot = new TreeItem<>(new MetaTopology("dummy", null));
        this.topologyTreeView.setShowRoot(false);
        this.topologyTreeView.setRoot(dummyRoot);

        for (GraphGroup graphGroup : this.graphGroups) {
            GraphGroupTreeItem graphGroupTreeItem = new GraphGroupTreeItem(this, graphGroup);
            graphGroupTreeItem.setGraphic(TopologyTreeItem.graphicForTreeItem(graphGroup));
            graphGroupTreeItem.setExpanded(true);
            dummyRoot.getChildren().add(graphGroupTreeItem);
            graphGroup.getGraphConfigurations().addListener(graphConfigurationListChangeListener(this, graphGroupTreeItem));
            for (GraphConfiguration graphConfiguration : graphGroup.getGraphConfigurations()) {
                GraphConfigurationTreeItem graphConfigurationTreeItem = new GraphConfigurationTreeItem(this, graphConfiguration);
                graphConfigurationTreeItem.setExpanded(false);
                graphGroupTreeItem.getChildren().add(graphConfigurationTreeItem);
            }
        }
        this.topologyTreeView.setCellFactory(ignore -> {
            SqlgTreeCellImpl sqlgTreeCell = new SqlgTreeCellImpl();


            sqlgTreeCell.setOnDragDetected(event -> {
                if (!(sqlgTreeCell.getItem() instanceof GraphGroup graphGroup)) {
                    return;
                }
                Dragboard db = sqlgTreeCell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString(graphGroup.getName());
                db.setContent(clipboardContent);
                event.consume();
            });
            sqlgTreeCell.setOnDragOver(event -> {
                if (event.getGestureSource() != sqlgTreeCell && event.getDragboard().hasString() &&
                        sqlgTreeCell.getItem() instanceof GraphGroup) {

                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });
            sqlgTreeCell.setOnDragEntered(event -> {
                if (event.getGestureSource() != sqlgTreeCell && event.getDragboard().hasString() &&
                        sqlgTreeCell.getItem() instanceof GraphGroup) {

                    sqlgTreeCell.getStyleClass().add("tree-cell-drop");
                }
                event.consume();
            });
            sqlgTreeCell.setOnDragExited(event -> {
                sqlgTreeCell.getStyleClass().remove("tree-cell-drop");
            });
            sqlgTreeCell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {

                    String _graphGroupToMove = event.getDragboard().getString();
                    GraphGroup graphGroupToDropOn = (GraphGroup) sqlgTreeCell.getItem();

                    GraphGroup graphGroupToMove = graphGroups.stream()
                            .filter(graphGroup -> graphGroup.getName().equals(_graphGroupToMove))
                            .findAny()
                            .orElseThrow();
                    TreeItem<ISqlgTopologyUI> graphGroupToMoveTreeItem = dummyRoot.getChildren().stream()
                            .filter(treeItem -> treeItem.getValue().getName().equals(_graphGroupToMove))
                            .findAny()
                            .orElseThrow();

                    int indexOf = graphGroups.indexOf(graphGroupToDropOn);
                    graphGroups.remove(graphGroupToMove);
                    graphGroups.add(indexOf, graphGroupToMove);

                    dummyRoot.getChildren().remove(graphGroupToMoveTreeItem);
                    dummyRoot.getChildren().add(indexOf, graphGroupToMoveTreeItem);
                    sqlgTreeCell.getStyleClass().remove("tree-cell-drop");
                    topologyTreeView.refresh();
                    User user = graphGroupToDropOn.getUser();
                    GraphGroup userGraphGroupToMove = user.getGraphGroups().stream()
                            .filter(graphGroup -> graphGroup.getName().equals(_graphGroupToMove))
                            .findAny()
                            .orElseThrow();
                    user.getGraphGroups().remove(userGraphGroupToMove);
                    user.getGraphGroups().add(indexOf, userGraphGroupToMove);

                    user.getRoot().persistConfig();

                    success = true;
                }
                /* let the source know whether the string was successfully
                 * transferred and used */
                event.setDropCompleted(success);
                event.consume();
            });
            sqlgTreeCell.setOnDragDone(event -> {
                if (event.getTransferMode() == TransferMode.MOVE) {
                }
                event.consume();
            });
            return sqlgTreeCell;
        });

        this.topologyTreeView.getSelectionModel().selectedItemProperty().addListener(
                (ignore1, ignore2, newValue) -> {
                    Parent view = null;
                    if (newValue != null) {
                        this.selectedTreeItem = newValue;
                        this.breadCrumbBar.setSelectedCrumb(this.selectedTreeItem);
                        int treeItemLevel = this.topologyTreeView.getTreeItemLevel(newValue);
                        if (treeItemLevel == GRAPH_GROUP_INDEX) {
                            Optional<GraphGroup> graphGroupOpt = this.graphGroups.stream().filter(g -> g.getName().equals(newValue.getValue().getName())).findAny();
                            if (graphGroupOpt.isPresent()) {
                                GraphConfigurationTableViewController graphConfigurationTableViewController = new GraphConfigurationTableViewController(
                                        this.primaryController.getStage(),
                                        this,
                                        graphGroupOpt.get(),
                                        graphGroupOpt.get().getGraphConfigurations()
                                );
                                view = graphConfigurationTableViewController.getView();
                            } else {
                                throw new IllegalStateException("Failed to find GraphGroup " + newValue.getValue());
                            }
                        } else if (treeItemLevel == GRAPH_CONFIGURATION_INDEX) {
                            GraphConfiguration graphConfiguration = (GraphConfiguration) newValue.getValue();
                            GraphConfigurationFormController graphConfigurationFormController = new GraphConfigurationFormController(
                                    getPrimaryController().getStage(),
                                    this,
                                    graphConfiguration
                            );
                            view = graphConfigurationFormController.getView();
                        } else if (treeItemLevel == META_SCHEMA_INDEX) {
                            MetaTopology metaTopology = (MetaTopology) newValue.getValue();
                            GraphConfiguration graphConfiguration = (GraphConfiguration) metaTopology.getParent();
                            SchemaTableViewController schemaTableViewController = new SchemaTableViewController(this, graphConfiguration);
                            view = schemaTableViewController.getView();
                        } else if (treeItemLevel == SCHEMA_INDEX) {
                            SchemaUI schemaUI = (SchemaUI) newValue.getValue();
                            SchemaFormController schemaFormController = new SchemaFormController(this, schemaUI);
                            view = schemaFormController.getView();
                        } else if (treeItemLevel == META_VERTEX_LABEL_INDEX) {
                            MetaTopology metaTopology = (MetaTopology) newValue.getValue();
                            SchemaUI schemaUI = (SchemaUI) metaTopology.getParent();
                            if (metaTopology.getName().equals(TopologyTreeItem.VERTEX_LABELS)) {
                                VertexLabelTableViewController vertexLabelTableViewController = new VertexLabelTableViewController(this, schemaUI);
                                view = vertexLabelTableViewController.getView();
                            } else if (metaTopology.getName().equals(TopologyTreeItem.EDGE_LABELS)) {
                                EdgeLabelTableViewController edgeLabelTableViewController = new EdgeLabelTableViewController(this, schemaUI);
                                view = edgeLabelTableViewController.getView();
                            } else {
                                throw new IllegalStateException(String.format("expected VertexLabelUI or EdgeLabelUI instead got '%s'", newValue.getValue().getClass().getSimpleName()));
                            }
                        } else if (treeItemLevel == VERTEX_LABEL_OR_EDGE_LABEL_INDEX) {
                            if (newValue.getValue() instanceof VertexLabelUI vertexLabelUI) {
                                VertexLabelFormController vertexLabelFormController = new VertexLabelFormController(this, vertexLabelUI);
                                view = vertexLabelFormController.getView();
                                this.pgDumpTab.updateView(vertexLabelUI.getSchemaUI().getGraphConfiguration(), vertexLabelUI.getVertexLabel());
                            } else if (newValue.getValue() instanceof EdgeLabelUI edgeLabelUI) {
                                EdgeLabelFormController edgeLabelFormController = new EdgeLabelFormController(this, edgeLabelUI);
                                view = edgeLabelFormController.getView();
                                this.pgDumpTab.updateView(edgeLabelUI.getSchemaUI().getGraphConfiguration(), edgeLabelUI.getEdgeLabel());
                            } else {
                                throw new IllegalStateException(String.format("expected VertexLabelUI or EdgeLabelUI instead got '%s'", newValue.getValue().getClass().getSimpleName()));
                            }
                        } else if (treeItemLevel == META_PROPERTY_COLUMN_OR_EDGE_LABEL_OR_INDEX_OR_PARTITIONS ||
                                treeItemLevel == META_PROPERTY_COLUMN_INDEX) {

                            MetaTopology metaTopology = (MetaTopology) newValue.getValue();

                            switch (metaTopology.getParent()) {
                                case VertexLabelUI vertexLabelUI -> {
                                    if (metaTopology.getName().equals(TopologyTreeItem.PROPERTY_COLUMNS)) {
                                        PropertyColumnTableViewController propertyColumnTableViewController = new PropertyColumnTableViewController(this, vertexLabelUI, null);
                                        view = propertyColumnTableViewController.getView();
                                    } else if (metaTopology.getName().equals(TopologyTreeItem.OUT_EDGE_ROLES)) {
                                        EdgeRoleTableViewController edgeRoleTableViewController = new EdgeRoleTableViewController(this, vertexLabelUI, null, Direction.OUT);
                                        view = edgeRoleTableViewController.getView();
                                    } else if (metaTopology.getName().equals(TopologyTreeItem.IN_EDGE_ROLES)) {
                                        EdgeRoleTableViewController edgeRoleTableViewController = new EdgeRoleTableViewController(this, vertexLabelUI, null, Direction.IN);
                                        view = edgeRoleTableViewController.getView();
                                    } else if (metaTopology.getName().equals(TopologyTreeItem.INDEXES)) {
                                        IndexTableViewController indexTableViewController = new IndexTableViewController(this, vertexLabelUI, null);
                                        view = indexTableViewController.getView();
                                    } else if (metaTopology.getName().equals(TopologyTreeItem.PARTITIONS)) {
                                        PartitionTableViewController propertyColumnTableViewController = new PartitionTableViewController(this, vertexLabelUI, null, null);
                                        view = propertyColumnTableViewController.getView();
                                    } else {
                                        throw new IllegalStateException(String.format("Unexpected metaTopology '%s'", metaTopology.getName()));
                                    }
                                }
                                case EdgeLabelUI edgeLabelUI -> {
                                    if (metaTopology.getName().equals(TopologyTreeItem.PROPERTY_COLUMNS)) {
                                        PropertyColumnTableViewController propertyColumnTableViewController = new PropertyColumnTableViewController(this, null, edgeLabelUI);
                                        view = propertyColumnTableViewController.getView();
                                    } else if (metaTopology.getName().equals(TopologyTreeItem.OUT_EDGE_ROLES)) {
                                        EdgeRoleTableViewController edgeRoleTableViewController = new EdgeRoleTableViewController(this, null, edgeLabelUI, Direction.OUT);
                                        view = edgeRoleTableViewController.getView();
                                    } else if (metaTopology.getName().equals(TopologyTreeItem.IN_EDGE_ROLES)) {
                                        EdgeRoleTableViewController edgeRoleTableViewController = new EdgeRoleTableViewController(this, null, edgeLabelUI, Direction.IN);
                                        view = edgeRoleTableViewController.getView();
                                    } else if (metaTopology.getName().equals(TopologyTreeItem.INDEXES)) {
                                        IndexTableViewController indexTableViewController = new IndexTableViewController(this, null, edgeLabelUI);
                                        view = indexTableViewController.getView();
                                    } else if (metaTopology.getName().equals(TopologyTreeItem.PARTITIONS)) {
                                        PartitionTableViewController propertyColumnTableViewController = new PartitionTableViewController(this, null, edgeLabelUI, null);
                                        view = propertyColumnTableViewController.getView();
                                    } else {
                                        throw new IllegalStateException(String.format("Unexpected metaTopology '%s'", metaTopology.getName()));
                                    }
                                }
                                case PartitionUI partitionUI -> {
                                    PartitionTableViewController propertyColumnTableViewController = new PartitionTableViewController(this, null, null, partitionUI);
                                    view = propertyColumnTableViewController.getView();
                                }
                                case null, default ->
                                        throw new IllegalStateException("Expected VertexLabelUI or EdgeLabelUI, instead got " + newValue.getValue().getClass().getSimpleName());
                            }
                        } else if (treeItemLevel == PROPERTY_COLUMN_OR_EDGE_LABEL_INDEX) {
                            if (newValue.getValue() instanceof PropertyColumnUI propertyColumnUI) {
                                PropertyColumnFormController propertyColumnFormController = new PropertyColumnFormController(this, propertyColumnUI);
                                view = propertyColumnFormController.getView();
                            } else if (newValue.getValue() instanceof IndexUI indexUI) {
                                IndexNameFormController indexFormController = new IndexNameFormController(this, indexUI);
                                view = indexFormController.getView();
                            } else if (newValue.getValue() instanceof EdgeRoleUI edgeRoleUI) {
                                EdgeRoleFormController edgeRoleFormController = new EdgeRoleFormController(this, edgeRoleUI);
                                view = edgeRoleFormController.getView();
                            } else if (newValue.getValue() instanceof PartitionUI partitionUI) {
                                PartitionFormController partitionFormController = new PartitionFormController(this, partitionUI);
                                view = partitionFormController.getView();
                            } else {
                                throw new IllegalStateException("not handled");
                            }
                        } else if (treeItemLevel == PROPERTY_COLUMN_INDEX_PARTITION) {
                            if (newValue.getValue() instanceof PropertyColumnUI propertyColumnUI) {
                                PropertyColumnFormController propertyColumnFormController = new PropertyColumnFormController(this, propertyColumnUI);
                                view = propertyColumnFormController.getView();
                            } else if (newValue.getValue() instanceof IndexUI indexUI) {
                                IndexNameFormController indexFormController = new IndexNameFormController(this, indexUI);
                                view = indexFormController.getView();
                            } else if (newValue.getValue() instanceof PartitionUI partitionUI) {
                                PartitionFormController partitionFormController = new PartitionFormController(this, partitionUI);
                                view = partitionFormController.getView();
                            } else {
                                throw new IllegalStateException("not handled");
                            }
                        } else {
                            if (treeItemLevel > 10 && isEven(treeItemLevel)) {
                                if (newValue.getValue() instanceof PartitionUI partitionUI) {
                                    PartitionFormController partitionFormController = new PartitionFormController(this, partitionUI);
                                    view = partitionFormController.getView();
                                } else {
                                    throw new IllegalStateException("not handled");
                                }
                            } else if (treeItemLevel > 10 && isOdd(treeItemLevel)) {
                                MetaTopology metaTopology = (MetaTopology) newValue.getValue();
                                if (metaTopology.getParent() instanceof PartitionUI partitionUI) {
                                    PartitionTableViewController propertyColumnTableViewController = new PartitionTableViewController(this, null, null, partitionUI);
                                    view = propertyColumnTableViewController.getView();
                                } else {
                                    throw new IllegalStateException("not handled");
                                }
                            } else {
                                throw new IllegalStateException("not handled");
                            }
                        }
                    }
                    if (view != null) {
                        this.viewTab.setContent(view);
                    }
                }
        );
    }

    public PrimaryController getPrimaryController() {
        return primaryController;
    }

    public void addPropertyColumn(
            GraphGroup graphGroup,
            GraphConfiguration graphConfiguration,
            Schema schema,
            AbstractLabel abstractLabel,
            PropertyColumn propertyColumn) {

        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, STR."\{graphConfiguration.getName()}.\{schema.getName()}");
                if (schemaTreeItem != null) {
                    if (abstractLabel instanceof VertexLabel vertexLabel) {
                        TreeItem<ISqlgTopologyUI> metaTopologyVertexLabelsTreeItem = search(schemaTreeItem, TopologyTreeItem.VERTEX_LABELS);
                        if (metaTopologyVertexLabelsTreeItem != null) {
                            TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(schemaTreeItem, STR."\{graphConfiguration.getName()}.\{schema.getName()}.\{vertexLabel.getName()}");
                            if (vertexLabelTreeItem != null) {
                                VertexLabelUI vertexLabelUI = (VertexLabelUI) vertexLabelTreeItem.getValue();
                                PropertyColumnUI propertyColumnUI = new PropertyColumnUI(vertexLabelUI, null, propertyColumn);
                                vertexLabelUI.getPropertyColumnUIs().add(propertyColumnUI);
                                TreeItem<ISqlgTopologyUI> metaTopologyPropertyColumnsTreeItem = search(vertexLabelTreeItem, TopologyTreeItem.PROPERTY_COLUMNS);
                                if (metaTopologyPropertyColumnsTreeItem != null) {
                                    metaTopologyPropertyColumnsTreeItem.getChildren().add(new TopologyTreeItem(propertyColumnUI));
                                }
                            }
                        }
                    } else {
                        TreeItem<ISqlgTopologyUI> metaTopologyEdgeLabelsTreeItem = search(schemaTreeItem, TopologyTreeItem.EDGE_LABELS);
                        if (metaTopologyEdgeLabelsTreeItem != null) {
                            TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(schemaTreeItem, STR."\{graphConfiguration.getName()}.\{schema.getName()}.\{abstractLabel.getName()}");
                            if (edgeLabelTreeItem != null) {
                                EdgeLabelUI edgeLabelUI = (EdgeLabelUI) edgeLabelTreeItem.getValue();
                                PropertyColumnUI propertyColumnUI = new PropertyColumnUI(null, edgeLabelUI, propertyColumn);
                                edgeLabelUI.getPropertyColumnUIs().add(propertyColumnUI);
                                TreeItem<ISqlgTopologyUI> metaTopologyPropertyColumnsTreeItem = search(edgeLabelTreeItem, TopologyTreeItem.PROPERTY_COLUMNS);
                                if (metaTopologyPropertyColumnsTreeItem != null) {
                                    metaTopologyPropertyColumnsTreeItem.getChildren().add(new TopologyTreeItem(propertyColumnUI));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void addEdgeRole(GraphGroup graphGroup, GraphConfiguration graphConfiguration, EdgeRole edgeRole) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, edgeRole.getEdgeLabel().getSchema().getName());
                if (schemaTreeItem != null) {
                    SchemaUI schemaUI = (SchemaUI) schemaTreeItem.getValue();
                    TreeItem<ISqlgTopologyUI> metaTopologyVertexLabels = search(schemaTreeItem, TopologyTreeItem.VERTEX_LABELS);
                    if (metaTopologyVertexLabels != null) {
                        VertexLabel vertexLabel = edgeRole.getVertexLabel();
                        TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(metaTopologyVertexLabels, vertexLabel.getLabel());
                        if (vertexLabelTreeItem != null) {
                            VertexLabelUI vertexLabelUI = (VertexLabelUI) vertexLabelTreeItem.getValue();
                            TreeItem<ISqlgTopologyUI> metaTopologyOutEdgeRoles = search(vertexLabelTreeItem, TopologyTreeItem.OUT_EDGE_ROLES);
                            if (metaTopologyOutEdgeRoles != null) {
                                TreeItem<ISqlgTopologyUI> outEdgeRoleTreeItem = search(metaTopologyOutEdgeRoles, edgeRole.getName());
                                if (outEdgeRoleTreeItem == null) {
                                    EdgeRoleUI edgeRoleUI = new EdgeRoleUI(vertexLabelUI, null, edgeRole);
                                    boolean success = vertexLabelUI.getOutEdgeRoleUIs().add(edgeRoleUI);
                                    LOGGER.info("addEdgeRole edgeRole[OUT] to ui {}/{}/{} with success = {}", graphGroup.getName(), schemaUI.getSchema().getName(), edgeRoleUI.getName(), success);
                                    success = metaTopologyOutEdgeRoles.getChildren().add(new TopologyTreeItem(edgeRoleUI));
                                    LOGGER.info("addEdgeLabel edgeRole[OUT] to ui {}/{}/{} with success = {}", graphGroup.getName(), schemaUI.getSchema().getName(), edgeRoleUI.getName(), success);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void addEdgeLabel(GraphGroup graphGroup, GraphConfiguration graphConfiguration, EdgeLabel edgeLabel) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                for (VertexLabel outVertexLabel : edgeLabel.getOutVertexLabels()) {
                    Schema outVertexLabelSchema = outVertexLabel.getSchema();
                    TreeItem<ISqlgTopologyUI> outVertexLabelSchemaTreeItem = search(graphConfigurationTreeItem, outVertexLabelSchema.getName());
                    if (outVertexLabelSchemaTreeItem != null) {
                        TreeItem<ISqlgTopologyUI> metaTopologyVertexLabel = search(outVertexLabelSchemaTreeItem, TopologyTreeItem.VERTEX_LABELS);
                        if (metaTopologyVertexLabel != null) {
                            TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(metaTopologyVertexLabel, outVertexLabel.getName());
                            if (vertexLabelTreeItem != null) {
                                VertexLabelUI vertexLabelUI = (VertexLabelUI) vertexLabelTreeItem.getValue();
                                TreeItem<ISqlgTopologyUI> metaTopologyOutEdgeRoles = search(vertexLabelTreeItem, TopologyTreeItem.OUT_EDGE_ROLES);
                                if (metaTopologyOutEdgeRoles != null) {
                                    EdgeRole edgeRole = edgeLabel.getOutEdgeRoles(outVertexLabel);
                                    TreeItem<ISqlgTopologyUI> outEdgeRoleTreeItem = search(metaTopologyOutEdgeRoles, edgeRole.getName());
                                    if (outEdgeRoleTreeItem == null) {
                                        EdgeRoleUI edgeRoleUI = new EdgeRoleUI(vertexLabelUI, null, edgeRole);
                                        boolean success = vertexLabelUI.getOutEdgeRoleUIs().add(edgeRoleUI);
                                        LOGGER.info("addEdgeLabel edgeRole[OUT] to ui {}/{}/{} with success = {}", graphGroup.getName(), outVertexLabelSchema.getName(), edgeRoleUI.getName(), success);
                                        success = metaTopologyOutEdgeRoles.getChildren().add(new TopologyTreeItem(edgeRoleUI));
                                        LOGGER.info("addEdgeLabel edgeRole[OUT] to ui {}/{}/{} with success = {}", graphGroup.getName(), outVertexLabelSchema.getName(), edgeRoleUI.getName(), success);
                                    }
                                }
                            }
                        }
                    }
                }
                for (VertexLabel inVertexLabel : edgeLabel.getInVertexLabels()) {
                    Schema inVertexLabelSchema = inVertexLabel.getSchema();
                    TreeItem<ISqlgTopologyUI> inVertexLabelSchemaTreeItem = search(graphConfigurationTreeItem, inVertexLabelSchema.getName());
                    if (inVertexLabelSchemaTreeItem != null) {
                        TreeItem<ISqlgTopologyUI> metaTopologyVertexLabel = search(inVertexLabelSchemaTreeItem, TopologyTreeItem.VERTEX_LABELS);
                        if (metaTopologyVertexLabel != null) {
                            TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(metaTopologyVertexLabel, inVertexLabel.getName());
                            if (vertexLabelTreeItem != null) {
                                VertexLabelUI vertexLabelUI = (VertexLabelUI) vertexLabelTreeItem.getValue();
                                TreeItem<ISqlgTopologyUI> metaTopologyInEdgeRoles = search(inVertexLabelSchemaTreeItem, TopologyTreeItem.IN_EDGE_ROLES);
                                if (metaTopologyInEdgeRoles != null) {
                                    EdgeRole edgeRole = edgeLabel.getInEdgeRoles(inVertexLabel);
                                    TreeItem<ISqlgTopologyUI> inEdgeRoleTreeItem = search(metaTopologyInEdgeRoles, edgeRole.getName());
                                    if (inEdgeRoleTreeItem == null) {
                                        EdgeRoleUI edgeRoleUI = new EdgeRoleUI(vertexLabelUI, null, edgeRole);
                                        boolean success = vertexLabelUI.getInEdgeRoleUIs().add(edgeRoleUI);
                                        LOGGER.info("addEdgeLabel edgeRole[IN] to ui {}/{}/{} with success = {}", graphGroup.getName(), inVertexLabelSchema.getName(), edgeRoleUI.getName(), success);
                                        success = metaTopologyInEdgeRoles.getChildren().add(new TopologyTreeItem(edgeRoleUI));
                                        LOGGER.info("addEdgeLabel edgeRole[IN] to tree {}/{}/{} with success = {}", graphGroup.getName(), inVertexLabelSchema.getName(), edgeRoleUI.getName(), success);
                                    }
                                }
                            }
                        }
                    }
                }
                Schema schema = edgeLabel.getSchema();
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    SchemaUI schemaUI = (SchemaUI) schemaTreeItem.getValue();
                    TreeItem<ISqlgTopologyUI> metaTopologyEdgeLabels = search(schemaTreeItem, TopologyTreeItem.EDGE_LABELS);
                    if (metaTopologyEdgeLabels != null) {
                        TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(metaTopologyEdgeLabels, edgeLabel.getName());
                        if (edgeLabelTreeItem == null) {
                            EdgeLabelUI edgeLabelUI = new EdgeLabelUI(schemaUI, edgeLabel);
                            boolean success = schemaUI.getEdgeLabelUIS().add(edgeLabelUI);
                            LOGGER.info("addEdgeLabel to ui {}/{}/{} with success = {}", graphGroup.getName(), schema.getName(), edgeLabelUI.getName(), success);
                            success = metaTopologyEdgeLabels.getChildren().add(new TopologyTreeItem(edgeLabelUI));
                            LOGGER.info("addEdgeLabel to tree {}/{}/{} with success = {}", graphGroup.getName(), schema.getName(), edgeLabelUI.getName(), success);
                        }
                    }
                }
            }
        }
    }

    public void addVertexLabel(GraphGroup graphGroup, GraphConfiguration graphConfiguration, Schema schema, VertexLabel vertexLabel) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, STR."\{graphConfiguration.getName()}.\{schema.getName()}");
                if (schemaTreeItem != null) {
                    SchemaUI schemaUI = (SchemaUI) schemaTreeItem.getValue();
                    TreeItem<ISqlgTopologyUI> metaTopologyVertexLabels = search(schemaTreeItem, TopologyTreeItem.VERTEX_LABELS);
                    if (metaTopologyVertexLabels != null) {
                        TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(metaTopologyVertexLabels, vertexLabel.getName());
                        if (vertexLabelTreeItem == null) {
                            VertexLabelUI vertexLabelUI = new VertexLabelUI(schemaUI, vertexLabel);
                            boolean success = metaTopologyVertexLabels.getChildren().add(new TopologyTreeItem(vertexLabelUI));
                            LOGGER.info("addVertexLabel to tree {}/{}/{} with success = {}", graphGroup.getName(), vertexLabel.getSchema().getName(), vertexLabel.getName(), success);
                            success = schemaUI.getVertexLabelUIs().add(vertexLabelUI);
                            LOGGER.info("addVertexLabel to table {}/{}/{} with success = {}", graphGroup.getName(), vertexLabel.getSchema().getName(), vertexLabel.getName(), success);
                        }
                    }
                }
            }
        }
    }

    public void deleteSchema(GraphGroup graphGroup, GraphConfiguration graphConfiguration, Schema schema) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, STR."\{graphConfiguration.getName()}.\{schema.getName()}");
                if (schemaTreeItem != null) {
                    SchemaUI schemaUI = (SchemaUI) schemaTreeItem.getValue();
                    boolean success = schemaTreeItem.getParent().getChildren().remove(schemaTreeItem);
                    graphConfiguration.getSchemaUis().remove(schemaUI);
                    LOGGER.info("deleteSchema from tree {}/{} with success = {}", graphGroup.getName(), schema.getName(), success);
                }
            }
        }
    }

    public void removeOldAddUpdatedPropertyColumn(
            boolean beforeCommit,
            GraphGroup graphGroup,
            GraphConfiguration graphConfiguration,
            Schema schema,
            AbstractLabel abstractLabel,
            PropertyColumn oldPropertyColumn,
            PropertyColumn propertyColumn) {

        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    TreeItem<ISqlgTopologyUI> abstractLabelTreeItem = search(schemaTreeItem, abstractLabel.getName());
                    if (abstractLabelTreeItem != null) {
                        TreeItem<ISqlgTopologyUI> metaTopologyPropertyColumns = search(abstractLabelTreeItem, TopologyTreeItem.PROPERTY_COLUMNS);
                        if (metaTopologyPropertyColumns != null) {
                            ISqlgTopologyUI sqlgTopologyUI = abstractLabelTreeItem.getValue();
                            TreeItem<ISqlgTopologyUI> propertyColumnTreeItem = search(metaTopologyPropertyColumns, oldPropertyColumn.getName());
                            if (propertyColumnTreeItem != null) {
                                PropertyColumnUI oldPropertyColumnUI = (PropertyColumnUI) propertyColumnTreeItem.getValue();
                                PropertyColumnUI propertyColumnUI;
                                if (sqlgTopologyUI instanceof VertexLabelUI) {
                                    propertyColumnUI = new PropertyColumnUI((VertexLabelUI) sqlgTopologyUI, null, propertyColumn);
                                } else {
                                    assert sqlgTopologyUI instanceof EdgeLabelUI;
                                    propertyColumnUI = new PropertyColumnUI(null, (EdgeLabelUI) sqlgTopologyUI, propertyColumn);
                                }

                                //get the selected item here before removing and adding the propertyColumnUi
                                ISqlgTopologyUI sqlgTopologyUI1 = this.topologyTreeView.getSelectionModel().getSelectedItem().getValue();

                                metaTopologyPropertyColumns.getChildren().remove(propertyColumnTreeItem);
                                TopologyTreeItem topologyTreeItem = new TopologyTreeItem(propertyColumnUI);
                                boolean success = metaTopologyPropertyColumns.getChildren().add(topologyTreeItem);
                                LOGGER.info("refreshPropertyColumn to tree {}/{}/{}/{} with success = {}", graphGroup.getName(), abstractLabel.getSchema().getName(), abstractLabel.getName(), propertyColumn.getName(), success);
                                if (sqlgTopologyUI instanceof VertexLabelUI) {
                                    success = propertyColumnUI.getVertexLabelUI().getPropertyColumnUIs().remove(oldPropertyColumnUI);
                                    success = propertyColumnUI.getVertexLabelUI().getPropertyColumnUIs().add(propertyColumnUI);
                                } else {
                                    success = propertyColumnUI.getEdgeLabelUI().getPropertyColumnUIs().remove(oldPropertyColumnUI);
                                    success = propertyColumnUI.getEdgeLabelUI().getPropertyColumnUIs().add(propertyColumnUI);
                                }
                                LOGGER.info("refreshPropertyColumn to model {}/{}/{}/{} with success = {}", graphGroup.getName(), abstractLabel.getSchema().getName(), abstractLabel.getName(), propertyColumn.getName(), success);

                                //if the current selected item the property, then select it again.
                                //remove and adding it to the tree remove it as the selected item, so we will select it again.
                                //only select the graphConfiguration making the update, i.e. beforeCommit = true
                                if (beforeCommit && sqlgTopologyUI1 instanceof PropertyColumnUI) {
                                    this.topologyTreeView.getSelectionModel().select(topologyTreeItem);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void refreshVertexLabel(
            GraphGroup graphGroup,
            GraphConfiguration graphConfiguration,
            Schema schema,
            VertexLabel oldVertexLabel,
            VertexLabel vertexLabel) {

        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(schemaTreeItem, oldVertexLabel.getName());
                    if (vertexLabelTreeItem != null) {
                        VertexLabelUI vertexLabelUI = (VertexLabelUI) vertexLabelTreeItem.getValue();
                        vertexLabelUI.setVertexLabel(vertexLabel);
                    }
                }
            }
        }
    }

    public void refreshEdgeLabel(
            GraphGroup graphGroup,
            GraphConfiguration graphConfiguration,
            Schema schema,
            VertexLabel vertexLabel,
            EdgeLabel oldEdgeLabel,
            EdgeLabel edgeLabel) {

        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(schemaTreeItem, vertexLabel.getName());
                    if (vertexLabelTreeItem != null) {
                        TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(vertexLabelTreeItem, oldEdgeLabel.getName());
                        if (edgeLabelTreeItem != null) {
                            EdgeLabelUI edgeLabelUI = (EdgeLabelUI) edgeLabelTreeItem.getValue();
                            edgeLabelUI.setEdgeLabel(edgeLabel);
                        }
                    }
                }
            }
        }
    }

    public void refreshTree() {
        this.topologyTreeView.refresh();
    }

    public void deleteVertexLabel(GraphGroup graphGroup, GraphConfiguration graphConfiguration, Schema schema, VertexLabel vertexLabel) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, STR."\{graphConfiguration.getName()}.\{schema.getName()}");
                if (schemaTreeItem != null) {
                    SchemaUI schemaUI = (SchemaUI) schemaTreeItem.getValue();

                    //we do not use the regular search here as the VertexLabel's name has already been changed by the edit.
                    Set<TreeItem<ISqlgTopologyUI>> toRemove = new HashSet<>();
                    TreeItem<ISqlgTopologyUI> metaVertexLabelsTreeItem = search(schemaTreeItem, TopologyTreeItem.VERTEX_LABELS);
                    for (TreeItem<ISqlgTopologyUI> vertexLabelTreeItem : Objects.requireNonNull(metaVertexLabelsTreeItem).getChildren()) {
                        VertexLabelUI vertexLabelUI = (VertexLabelUI) vertexLabelTreeItem.getValue();
                        if (vertexLabelUI.getVertexLabel().getName().equals(vertexLabel.getName())) {
                            toRemove.add(vertexLabelTreeItem);
                            schemaUI.getVertexLabelUIs().remove(vertexLabelUI);
                        }
                    }
                    for (TreeItem<ISqlgTopologyUI> iSqlgTopologyUITreeItem : toRemove) {
                        metaVertexLabelsTreeItem.getChildren().remove(iSqlgTopologyUITreeItem);
                    }
                }
            }
        }
    }

    public void deleteEdgeRole(
            GraphGroup graphGroup,
            GraphConfiguration graphConfiguration,
            Schema schema,
            EdgeRole edgeRole,
            Direction direction) {

        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    TreeItem<ISqlgTopologyUI> metaTopologyVertexLabelsTreeItem = search(schemaTreeItem, TopologyTreeItem.VERTEX_LABELS);
                    if (metaTopologyVertexLabelsTreeItem != null) {
                        TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(metaTopologyVertexLabelsTreeItem, edgeRole.getVertexLabel().getName());
                        if (vertexLabelTreeItem != null) {
                            VertexLabelUI vertexLabelUI = (VertexLabelUI) vertexLabelTreeItem.getValue();
                            TreeItem<ISqlgTopologyUI> metaTopologyEdgeRoleTreeItem;
                            if (direction == Direction.OUT) {
                                metaTopologyEdgeRoleTreeItem = search(vertexLabelTreeItem, TopologyTreeItem.OUT_EDGE_ROLES);
                            } else {
                                metaTopologyEdgeRoleTreeItem = search(vertexLabelTreeItem, TopologyTreeItem.IN_EDGE_ROLES);
                            }
                            if (metaTopologyEdgeRoleTreeItem != null) {
                                TreeItem<ISqlgTopologyUI> edgeRoleTreeItem = search(metaTopologyEdgeRoleTreeItem, edgeRole.getName());
                                if (edgeRoleTreeItem != null) {
                                    EdgeRoleUI edgeRoleUI = (EdgeRoleUI) edgeRoleTreeItem.getValue();
                                    if (direction == Direction.OUT) {
                                        boolean success = vertexLabelUI.getOutEdgeRoleUIs().remove(edgeRoleUI);
                                        LOGGER.debug("deleteEdgeRole[{}] from ui model with {}/{}/{}/{}/{} with {}", direction.name(), graphGroup.getName(), graphConfiguration.getName(), schema.getName(), vertexLabelUI.getName(), edgeRole.getName(), success);
                                    } else {
                                        boolean success = vertexLabelUI.getInEdgeRoleUIs().remove(edgeRoleUI);
                                        LOGGER.debug("deleteEdgeRole[{}] from ui model with {}/{}/{}/{}/{} with {}", direction.name(), graphGroup.getName(), graphConfiguration.getName(), schema.getName(), vertexLabelUI.getName(), edgeRole.getName(), success);
                                    }
                                    boolean success = metaTopologyEdgeRoleTreeItem.getChildren().remove(edgeRoleTreeItem);
                                    LOGGER.debug("deleteEdgeRole[{}] from tree {}/{}/{}/{}/{} with {}", direction.name(), graphGroup.getName(), graphConfiguration.getName(), schema.getName(), vertexLabelUI.getName(), edgeRole.getName(), success);
                                }
                            }
                        }
                    }
                    TreeItem<ISqlgTopologyUI> metaTopologyEdgeLabelsTreeItem = search(schemaTreeItem, TopologyTreeItem.EDGE_LABELS);
                    if (metaTopologyEdgeLabelsTreeItem != null) {
                        TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(metaTopologyEdgeLabelsTreeItem, edgeRole.getEdgeLabel().getName());
                        if (edgeLabelTreeItem != null) {
                            EdgeLabelUI edgeLabelUI = (EdgeLabelUI) edgeLabelTreeItem.getValue();

                            TreeItem<ISqlgTopologyUI> metaTopologyEdgeRoleTreeItem;
                            if (direction == Direction.OUT) {
                                metaTopologyEdgeRoleTreeItem = search(edgeLabelTreeItem, TopologyTreeItem.OUT_EDGE_ROLES);
                            } else {
                                metaTopologyEdgeRoleTreeItem = search(edgeLabelTreeItem, TopologyTreeItem.IN_EDGE_ROLES);
                            }
                            if (metaTopologyEdgeRoleTreeItem != null) {
                                TreeItem<ISqlgTopologyUI> edgeRoleTreeItem = search(metaTopologyEdgeRoleTreeItem, edgeRole.getName());
                                if (edgeRoleTreeItem != null) {
                                    EdgeRoleUI edgeRoleUI = (EdgeRoleUI) edgeRoleTreeItem.getValue();
                                    if (direction == Direction.OUT) {
                                        boolean success = edgeLabelUI.getOutEdgeRoleUIs().remove(edgeRoleUI);
                                        LOGGER.debug("deleteEdgeRole[{}] from ui model with {}/{}/{}/{}/{} with {}", direction.name(), graphGroup.getName(), graphConfiguration.getName(), schema.getName(), edgeLabelUI.getName(), edgeRole.getName(), success);
                                    } else {
                                        boolean success = edgeLabelUI.getInEdgeRoleUIs().remove(edgeRoleUI);
                                        LOGGER.debug("deleteEdgeRole[{}] from ui model with {}/{}/{}/{}/{} with {}", direction.name(), graphGroup.getName(), graphConfiguration.getName(), schema.getName(), edgeLabelUI.getName(), edgeRole.getName(), success);
                                    }
                                    boolean success = metaTopologyEdgeRoleTreeItem.getChildren().remove(edgeRoleTreeItem);
                                    LOGGER.debug("deleteEdgeRole[{}] from tree {}/{}/{}/{}/{} with {}", direction.name(), graphGroup.getName(), graphConfiguration.getName(), schema.getName(), edgeLabelUI.getName(), edgeRole.getName(), success);
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    public void deleteEdgeLabel(GraphGroup graphGroup, GraphConfiguration graphConfiguration, Schema schema, EdgeLabel edgeLabel) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    SchemaUI schemaUI = (SchemaUI) schemaTreeItem.getValue();
                    TreeItem<ISqlgTopologyUI> metaTopologyEdgeLabelsTreeItem = search(schemaTreeItem, TopologyTreeItem.EDGE_LABELS);
                    if (metaTopologyEdgeLabelsTreeItem != null) {


                        //we do not use the regular search here as the VertexLabel's name has already been changed by the edit.
                        Set<TreeItem<ISqlgTopologyUI>> toRemove = new HashSet<>();
                        for (TreeItem<ISqlgTopologyUI> edgeLabelTreeItem : Objects.requireNonNull(metaTopologyEdgeLabelsTreeItem).getChildren()) {
                            EdgeLabelUI edgeLabelUI = (EdgeLabelUI) edgeLabelTreeItem.getValue();
                            if (edgeLabelUI.getEdgeLabel().getName().equals(edgeLabel.getName())) {
                                toRemove.add(edgeLabelTreeItem);
                                schemaUI.getEdgeLabelUIS().remove(edgeLabelUI);
                            }
                        }
                        for (TreeItem<ISqlgTopologyUI> iSqlgTopologyUITreeItem : toRemove) {
                            metaTopologyEdgeLabelsTreeItem.getChildren().remove(iSqlgTopologyUITreeItem);
                        }
                    }
                }
            }
        }
    }

    public void deletePropertyColumn(
            GraphGroup graphGroup,
            GraphConfiguration graphConfiguration,
            Schema schema,
            VertexLabel vertexLabel,
            EdgeLabel edgeLabel,
            PropertyColumn propertyColumn) {

        assert (vertexLabel != null && edgeLabel == null) || (vertexLabel == null && edgeLabel != null);
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, STR."\{graphConfiguration.getName()}.\{schema.getName()}");
                if (schemaTreeItem != null) {
                    if (edgeLabel != null) {
                        TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(schemaTreeItem, STR."\{graphConfiguration.getName()}.\{schema.getName()}.\{edgeLabel.getName()}");
                        if (edgeLabelTreeItem != null) {
                            TreeItem<ISqlgTopologyUI> metaPropertyColumnsTreeItem = search(edgeLabelTreeItem, TopologyTreeItem.PROPERTY_COLUMNS);
                            if (metaPropertyColumnsTreeItem != null) {
                                //we do not use the regular search here as the PropertyColumnUI's name has already been changed by the edit.
                                Set<TreeItem<ISqlgTopologyUI>> toRemove = new HashSet<>();
                                for (TreeItem<ISqlgTopologyUI> propertyColumnTreeItem : metaPropertyColumnsTreeItem.getChildren()) {
                                    PropertyColumnUI propertyColumnUI = (PropertyColumnUI) propertyColumnTreeItem.getValue();
                                    if (propertyColumnUI.getPropertyColumn().getName().equals(propertyColumn.getName())) {
                                        toRemove.add(propertyColumnTreeItem);
                                        propertyColumnUI.getEdgeLabelUI().getPropertyColumnUIs().remove(propertyColumnUI);
                                    }
                                }
                                for (TreeItem<ISqlgTopologyUI> iSqlgTopologyUITreeItem : toRemove) {
                                    metaPropertyColumnsTreeItem.getChildren().remove(iSqlgTopologyUITreeItem);
                                }
                            }
                        }
                    } else {
                        TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(schemaTreeItem, STR."\{graphConfiguration.getName()}.\{schema.getName()}.\{vertexLabel.getName()}");
                        if (vertexLabelTreeItem != null) {
                            TreeItem<ISqlgTopologyUI> metaPropertyColumnsTreeItem = search(vertexLabelTreeItem, TopologyTreeItem.PROPERTY_COLUMNS);
                            if (metaPropertyColumnsTreeItem != null) {
                                //we do not use the regular search here as the PropertyColumnUI's name has already been changed by the edit.
                                Set<TreeItem<ISqlgTopologyUI>> toRemove = new HashSet<>();
                                for (TreeItem<ISqlgTopologyUI> propertyColumnTreeItem : metaPropertyColumnsTreeItem.getChildren()) {
                                    PropertyColumnUI propertyColumnUI = (PropertyColumnUI) propertyColumnTreeItem.getValue();
                                    if (propertyColumnUI.getPropertyColumn().getName().equals(propertyColumn.getName())) {
                                        toRemove.add(propertyColumnTreeItem);
                                        propertyColumnUI.getVertexLabelUI().getPropertyColumnUIs().remove(propertyColumnUI);
                                    }
                                }
                                for (TreeItem<ISqlgTopologyUI> iSqlgTopologyUITreeItem : toRemove) {
                                    metaPropertyColumnsTreeItem.getChildren().remove(iSqlgTopologyUITreeItem);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void deleteIndex(
            GraphGroup graphGroup,
            GraphConfiguration graphConfiguration,
            Schema schema,
            VertexLabel vertexLabel,
            EdgeLabel edgeLabel,
            Index index) {

        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(schemaTreeItem, vertexLabel.getName());
                    if (vertexLabelTreeItem != null) {
                        if (edgeLabel != null) {
                            TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(vertexLabelTreeItem, edgeLabel.getName());
                            if (edgeLabelTreeItem != null) {
                                TreeItem<ISqlgTopologyUI> indexTreeItem = search(vertexLabelTreeItem, index.getName());
                                if (indexTreeItem != null) {
                                    IndexUI indexUI = (IndexUI) indexTreeItem.getValue();
                                    indexUI.getEdgeLabelUI().getIndexUIs().remove(indexUI);
                                    indexTreeItem.getParent().getChildren().remove(indexTreeItem);
                                }
                            }
                        } else {
                            TreeItem<ISqlgTopologyUI> indexTreeItem = search(vertexLabelTreeItem, index.getName());
                            if (indexTreeItem != null) {
                                IndexUI indexUI = (IndexUI) indexTreeItem.getValue();
                                indexUI.getVertexLabelUI().getIndexUIs().remove(indexUI);
                                indexTreeItem.getParent().getChildren().remove(indexTreeItem);
                            }
                        }
                    }
                }
            }
        }
    }

    public void deletePartition(GraphGroup graphGroup, GraphConfiguration graphConfiguration, Schema schema, VertexLabel vertexLabel, EdgeLabel edgeLabel, Partition partition) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(schemaTreeItem, vertexLabel.getName());
                    if (vertexLabelTreeItem != null) {
                        if (edgeLabel != null) {
                            throw new IllegalStateException("todo");
//                            TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(vertexLabelTreeItem, edgeLabel.getName());
//                            if (edgeLabelTreeItem != null) {
//                                TreeItem<ISqlgTopologyUI> indexTreeItem = search(vertexLabelTreeItem, index.getName());
//                                if (indexTreeItem != null) {
//                                    indexTreeItem.getParent().getChildren().remove(indexTreeItem);
//                                }
//                            }
                        } else {


                            TreeItem<ISqlgTopologyUI> partitionTreeItem = search(vertexLabelTreeItem, partition.getName());
                            if (partitionTreeItem != null) {
                                PartitionUI partitionUI = (PartitionUI) partitionTreeItem.getValue();
                                TreeItem<ISqlgTopologyUI> metaPartition = partitionTreeItem.getParent();
                                metaPartition.getChildren().remove(partitionTreeItem);
                                if (metaPartition.getValue().getName().equals("Partitions")) {
                                    VertexLabelUI vertexLabelUI = (VertexLabelUI) vertexLabelTreeItem.getValue();
                                    vertexLabelUI.getPartitionUIs().remove(partitionUI);
                                } else if (metaPartition.getValue().getName().equals("Sub partitions")) {
                                    MetaTopology subPartitionMetaTopology = (MetaTopology) metaPartition.getValue();
                                    PartitionUI subParitionUI = (PartitionUI) subPartitionMetaTopology.getParent();
                                    subParitionUI.getSubPartitionUIs().remove(partitionUI);
                                }
                            }

//                            TreeItem<ISqlgTopologyUI> metaPartitionsTreeItem = search(vertexLabelTreeItem, TopologyTreeItem.PARTITIONS);
//                            if (metaPartitionsTreeItem != null) {
//                                //we do not use the regular search here as the PartitionUI's name has already been changed but the edit.
//                                Set<TreeItem<ISqlgTopologyUI>> toRemove = new HashSet<>();
//                                for (TreeItem<ISqlgTopologyUI> partitionTreeItem : metaPartitionsTreeItem.getChildren()) {
//                                    PartitionUI partitionColumnUI = (PartitionUI) partitionTreeItem.getValue();
//                                    if (partitionColumnUI.getPartition().getName().equals(partition.getName())) {
//                                        toRemove.add(partitionTreeItem);
//                                        partitionColumnUI.getVertexLabelUI().getPartitionUIs().remove(partitionColumnUI);
//                                    }
//                                }
//                                for (TreeItem<ISqlgTopologyUI> iSqlgTopologyUITreeItem : toRemove) {
//                                    metaPartitionsTreeItem.getChildren().remove(iSqlgTopologyUITreeItem);
//                                }
//                            }
                        }
                    }
                }
            }
        }
    }

    private TreeItem<ISqlgTopologyUI> search(TreeItem<ISqlgTopologyUI> root, String value) {
        Queue<TreeItem<ISqlgTopologyUI>> queue = new ArrayDeque<>(root.getChildren());
        while (!queue.isEmpty()) {
            TreeItem<ISqlgTopologyUI> currentNode = queue.remove();
            if (currentNode.getValue().getQualifiedName().equals(value)) {
                return currentNode;
            } else {
                ISqlgTopologyUI sqlgTreeData = currentNode.getValue();
                if (!(sqlgTreeData instanceof GraphConfiguration graphConfiguration) || graphConfiguration.isOpen()) {
                    queue.addAll(currentNode.getChildren());
                }
            }
        }
        return null;
    }

    private final class SqlgTreeCellImpl extends TreeCell<ISqlgTopologyUI> {

        private TextField textField;
        private final ProgressBar progressBar = new ProgressBar();

        public SqlgTreeCellImpl() {
            super();
        }

        @Override
        public void startEdit() {
            if (getItem() instanceof GraphConfiguration) {
                super.startEdit();
                if (getItem() instanceof GraphConfiguration && this.textField == null) {
                    createTextField();
                }
                setText(null);
                setGraphic(this.textField);
                this.textField.selectAll();
                this.textField.requestFocus();
            } else {
                cancelEdit();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().getName());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void commitEdit(ISqlgTopologyUI newValue) {
            super.commitEdit(newValue);
            getTreeItem().setValue(newValue);
        }

        @Override
        public void updateItem(ISqlgTopologyUI item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (this.textField != null) {
                        this.textField.setText(getItem().getName());
                    }
                } else {
                    TreeItem<ISqlgTopologyUI> treeItem = getTreeItem();
                    if (treeItem instanceof GraphConfigurationTreeItem graphConfigurationTreeItem && graphConfigurationTreeItem.isLoading()) {
                        setText(null);
                        setGraphic(this.progressBar);
                    } else if (treeItem instanceof GraphGroupTreeItem graphGroupTreeItem) {
                        setText(getItem().getName());
                        setGraphic(getTreeItem().getGraphic());
                        getStyleClass().add("tree-item-hover");
                        final ContextMenu contextMenu = new ContextMenu();
                        MenuItem addGraphMenu = new MenuItem("Add graph group");
                        addGraphMenu.setGraphic(Fontawesome.PLUS.label(Solid));
                        addGraphMenu.setOnAction(ignore -> {
                            GraphGroup graphGroup = primaryController.addDefaultGraphGroup();
                            TreeItem<ISqlgTopologyUI> _graphGroupTreeItem = new TreeItem<>(graphGroup);
                            _graphGroupTreeItem.setGraphic(Fontawesome.BARS.label(Solid));
                            graphGroup.getGraphConfigurations().addListener(graphConfigurationListChangeListener(LeftPaneController.this, graphGroupTreeItem));
                            _graphGroupTreeItem.setExpanded(false);
                            topologyTreeView.getRoot().getChildren().add(_graphGroupTreeItem);
                        });
                        MenuItem deleteGraphMenu = new MenuItem("Delete graph group");
                        deleteGraphMenu.setGraphic(Fontawesome.MINUS.label(Solid));
                        deleteGraphMenu.setUserData(graphGroupTreeItem);
                        deleteGraphMenu.setOnAction(ev -> {
                            MenuItem menuItem = (MenuItem) ev.getSource();
                            GraphGroupTreeItem _graphGroupTreeItem = (GraphGroupTreeItem) menuItem.getUserData();
                            GraphGroup graphGroup = (GraphGroup) _graphGroupTreeItem.getValue();
                            User user = graphGroup.getUser();
                            user.getGraphGroups().remove(graphGroup);
                            TreeItem<ISqlgTopologyUI> root = LeftPaneController.this.topologyTreeView.getRoot();
                            root.getChildren().remove(_graphGroupTreeItem);
                            user.getRoot().persistConfig();
                        });
                        contextMenu.getItems().addAll(addGraphMenu, deleteGraphMenu);
                        setContextMenu(contextMenu);
                    } else if (treeItem instanceof GraphConfigurationTreeItem graphConfigurationTreeItem) {
                        GraphConfiguration graphConfiguration = (GraphConfiguration) graphConfigurationTreeItem.getValue();

                        Button close = new Button();
                        close.disableProperty().bind(Bindings.createBooleanBinding(() -> !graphConfiguration.isOpen(), graphConfiguration.sqlgGraphOpenPropertyProperty()));
                        close.setGraphic(Fontawesome.XMARK.label(Solid));
                        Button refresh = new Button();
                        refresh.disableProperty().bind(Bindings.createBooleanBinding(() -> !graphConfiguration.isOpen(), graphConfiguration.sqlgGraphOpenPropertyProperty()));
                        refresh.graphicProperty().bind(Bindings.createObjectBinding(
                                        () -> {
                                            if (graphConfiguration.isRefreshTopologyProperty()) {
                                                return Fontawesome.ARROWS_ROTATE_RIGHT.label(Solid, 14, "font-awesome-green");
                                            } else {
                                                return Fontawesome.ARROWS_ROTATE_RIGHT.label(Solid, 14);
                                            }
                                        },
                                        graphConfiguration.refreshTopologyPropertyProperty()
                                )
                        );

                        HBox hBox = new HBox(5);
                        Insets closeInsets = new Insets(1, 4, 1, 4);
                        Insets refreshInsets = new Insets(1, 4, 1, 4);
                        refresh.setPadding(refreshInsets);
                        close.setPadding(closeInsets);
                        hBox.getChildren().addAll(Fontawesome.DATABASE.label(Solid), close, refresh);
                        setGraphic(hBox);
                        setText(getItem().getName());
                        close.setOnAction(ignore -> {
                            graphConfiguration.closeSqlgGraph();
                            graphConfigurationTreeItem.closeGraph();
                            graphConfigurationTreeItem.setExpanded(false);
                        });
                        refresh.setOnAction(ignore -> {
                            graphConfiguration.refreshSqlgGraph();
                            graphConfigurationTreeItem.refreshGraph();
                            graphConfigurationTreeItem.setExpanded(false);
                            graphConfiguration.refreshTopologyPropertyProperty().set(false);
                        });

                        final ContextMenu contextMenu = new ContextMenu();
                        MenuItem addGremlinTab = new MenuItem(STR."Open query \{getTreeItem().getValue().getName()}");
                        addGremlinTab.setGraphic(Fontawesome.PLUS.label(Solid));
                        addGremlinTab.setOnAction(ignore -> {
                            primaryController.addGremlinTab(graphConfiguration);
                        });

                        contextMenu.getItems().add(addGremlinTab);
                        setContextMenu(contextMenu);
                        setText(getItem().getName());

                    } else {
                        setText(getItem().getName());
                        setGraphic(getTreeItem().getGraphic());
                        setContextMenu(null);
                    }
                }
            }
        }

        private void createTextField() {
            this.textField = new TextField();
            this.textField.setText(getItem().getName());
            setText(null);
            this.textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                } else if (event.getCode() == KeyCode.ENTER) {
                    LeftPaneController.this.selectedTreeItem.getValue().nameProperty().set(textField.textProperty().getValue());
                    commitEdit(LeftPaneController.this.selectedTreeItem.getValue());
                    event.consume();
                }
            });
        }
    }

    public void selectPropertyColumn(GraphGroup graphGroup, GraphConfiguration graphConfiguration, Schema schema, AbstractLabel abstractLabel, String propertyColumnName) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    TreeItem<ISqlgTopologyUI> abstractLabelTreeItem = search(schemaTreeItem, abstractLabel.getName());
                    if (abstractLabelTreeItem != null) {
                        TreeItem<ISqlgTopologyUI> propertyColumnTreeItem = search(abstractLabelTreeItem, propertyColumnName);
                        if (propertyColumnTreeItem != null) {
                            this.topologyTreeView.getSelectionModel().select(propertyColumnTreeItem);
                        }
                    }
                }
            }
        }
    }

    public void selectVertexLabel(GraphGroup graphGroup, GraphConfiguration graphConfiguration, Schema schema, String vertexLabelName) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(schemaTreeItem, vertexLabelName);
                    if (vertexLabelTreeItem != null) {
                        this.topologyTreeView.getSelectionModel().select(vertexLabelTreeItem);
                    }
                }
            }
        }
    }

    public void selectEdgeLabel(GraphGroup graphGroup, GraphConfiguration graphConfiguration, Schema schema, String edgeLabelName) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(schemaTreeItem, edgeLabelName);
                    if (edgeLabelTreeItem != null) {
                        this.topologyTreeView.getSelectionModel().select(edgeLabelTreeItem);
                    }
                }
            }
        }
    }

    static boolean isEven(int x) {
        return (x | 1) > x;
    }

    static boolean isOdd(int x) {
        return (x | 1) == x;
    }

}
