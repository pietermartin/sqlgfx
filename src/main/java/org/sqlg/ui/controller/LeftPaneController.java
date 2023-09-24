package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.App;
import org.sqlg.ui.GraphConfigurationTreeItem;
import org.sqlg.ui.TopologyTreeItem;
import org.sqlg.ui.model.*;
import org.umlg.sqlg.structure.topology.*;

import java.io.IOException;
import java.util.*;

public class LeftPaneController {

    private final static Logger LOGGER = LoggerFactory.getLogger(LeftPaneController.class);
    private final PrimaryController2 primaryController;
    private TreeView<ISqlgTopologyUI> topologyTreeView;
    private TreeItem<ISqlgTopologyUI> selectedTreeItem;
    private final ObservableList<GraphGroup> graphGroups;
    private final AnchorPane leftAnchorPane;
    private final AnchorPane rightAnchorPane;
    private final EventHandler<TreeItem.TreeModificationEvent<ISqlgTopologyUI>> graphConfigurationExpandingEventHandles;

    private final int GRAPH_GROUP_INDEX = 1;
    private final int GRAPH_CONFIGURATION_INDEX = 2;
    private final int META_SCHEMA_INDEX = 3;
    private final int SCHEMA_INDEX = 4;
    private final int META_VERTEX_LABEL_INDEX = 5;
    private final int VERTEX_LABEL_OR_EDGE_LABEL_INDEX = 6;
    private final int META_PROPERTY_COLUMN_OR_EDGE_LABEL_INDEX = 7;
    private final int PROPERTY_COLUMN_OR_EDGE_LABEL_INDEX = 8;
    private final int META_PROPERTY_COLUMN_INDEX = 9;
    private final int PROPERTY_COLUMN_INDEX = 10;

    public LeftPaneController(
            PrimaryController2 primaryController,
            ObservableList<GraphGroup> graphGroups,
            AnchorPane leftAnchorPane,
            AnchorPane rightAnchorPane) {

        this.primaryController = primaryController;
        this.graphGroups = graphGroups;
        this.leftAnchorPane = leftAnchorPane;
//        this.leftAnchorPane.setBorder(Border.EMPTY);
//        this.leftAnchorPane.setPadding(new Insets(5, 5, 5, 5));
        this.rightAnchorPane = rightAnchorPane;
        this.rightAnchorPane.setPadding(Insets.EMPTY);
//        this.rightAnchorPane.setPadding(new Insets(5, 5, 5, 5));
        this.graphConfigurationExpandingEventHandles = event -> {
        };
    }

    protected void initialize() {
        this.topologyTreeView = new TreeView<>();
//        this.topologyTreeView.setStyle("-fx-border-color: transparent;");
        AnchorPane.setLeftAnchor(this.topologyTreeView, 0D);
        AnchorPane.setTopAnchor(this.topologyTreeView, 0D);
        AnchorPane.setRightAnchor(this.topologyTreeView, 0D);
        AnchorPane.setBottomAnchor(this.topologyTreeView, 0D);

        this.leftAnchorPane.getChildren().clear();
        this.leftAnchorPane.getChildren().add(this.topologyTreeView);

        this.topologyTreeView.setEditable(true);
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem addGraphMenu = new MenuItem("Add graph group");
        contextMenu.getItems().addAll(addGraphMenu);
        this.topologyTreeView.setContextMenu(contextMenu);

        TreeItem<ISqlgTopologyUI> dummyRoot = new TreeItem<>(new MetaTopology("dummy", null));
        this.topologyTreeView.setShowRoot(false);
        this.topologyTreeView.setRoot(dummyRoot);

        for (GraphGroup graphGroup : graphGroups) {
            TreeItem<ISqlgTopologyUI> graphGroupTreeItem = new TreeItem<>(graphGroup);
            GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
            Glyph glyph = fontAwesome.create(FontAwesome.Glyph.GROUP).size(12);
            graphGroupTreeItem.setGraphic(glyph);
            graphGroupTreeItem.setExpanded(true);
            dummyRoot.getChildren().add(graphGroupTreeItem);

            graphGroup.getGraphConfigurations().addListener((ListChangeListener<GraphConfiguration>) c -> {
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
                                TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = new GraphConfigurationTreeItem(this, addItem);
                                graphGroupTreeItem.getChildren().add(graphConfigurationTreeItem);
                            }
                        }
                }
            });

            for (GraphConfiguration graphConfiguration : graphGroup.getGraphConfigurations()) {
                GraphConfigurationTreeItem graphConfigurationTreeItem = new GraphConfigurationTreeItem(this, graphConfiguration);
                graphConfigurationTreeItem.setExpanded(false);
                graphGroupTreeItem.getChildren().add(graphConfigurationTreeItem);
                graphConfigurationTreeItem.addEventHandler(TreeItem.branchExpandedEvent(), this.graphConfigurationExpandingEventHandles);
            }
        }
        this.topologyTreeView.setCellFactory(treeView -> new SqlgTreeCellImpl());

        this.topologyTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    Parent view = null;
                    if (newValue != null) {
                        this.selectedTreeItem = newValue;
                        if (this.topologyTreeView.getTreeItemLevel(newValue) == GRAPH_GROUP_INDEX) {
                            Optional<GraphGroup> graphGroupOpt = this.graphGroups.stream().filter(g -> g.getName().equals(newValue.getValue().getName())).findAny();
                            if (graphGroupOpt.isPresent()) {
                                FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("graphTableView.fxml"));
                                try {
                                    fxmlLoader.setControllerFactory(controllerClass -> new GraphTableViewController(this, graphGroupOpt.get(), graphGroupOpt.get().getGraphConfigurations()));
                                    view = fxmlLoader.load();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                throw new IllegalStateException("Failed to find GraphGroup " + newValue.getValue());
                            }
                        } else if (this.topologyTreeView.getTreeItemLevel(newValue) == GRAPH_CONFIGURATION_INDEX) {
                            GraphConfiguration graphConfiguration = (GraphConfiguration) newValue.getValue();
                            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("graphFormView.fxml"));
                            try {
                                fxmlLoader.setControllerFactory(controllerClass -> new GraphFormController(this, graphConfiguration));
                                view = fxmlLoader.load();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (this.topologyTreeView.getTreeItemLevel(newValue) == META_SCHEMA_INDEX) {
                            MetaTopology metaTopology = (MetaTopology) newValue.getValue();
                            GraphConfiguration graphConfiguration = (GraphConfiguration) metaTopology.getParent();
                            SchemaTableViewController schemaTableViewController = new SchemaTableViewController(this, graphConfiguration);
                            view = schemaTableViewController.getView();
                        } else if (this.topologyTreeView.getTreeItemLevel(newValue) == SCHEMA_INDEX) {
                            SchemaUI schemaUI = (SchemaUI) newValue.getValue();
                            SchemaFormController schemaFormController = new SchemaFormController(this, schemaUI);
                            view = schemaFormController.getView();
                        } else if (this.topologyTreeView.getTreeItemLevel(newValue) == META_VERTEX_LABEL_INDEX) {
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
                        } else if (this.topologyTreeView.getTreeItemLevel(newValue) == VERTEX_LABEL_OR_EDGE_LABEL_INDEX) {
                            if (newValue.getValue() instanceof VertexLabelUI vertexLabelUI) {
                                VertexLabelFormController vertexLabelFormController = new VertexLabelFormController(this, vertexLabelUI);
                                view = vertexLabelFormController.getView();
                            } else if (newValue.getValue() instanceof EdgeLabelUI edgeLabelUI) {
                                EdgeLabelFormController edgeLabelFormController = new EdgeLabelFormController(this, edgeLabelUI);
                                view = edgeLabelFormController.getView();
                            } else {
                                throw new IllegalStateException(String.format("expected VertexLabelUI or EdgeLabelUI instead got '%s'", newValue.getValue().getClass().getSimpleName()));
                            }
                        } else if (this.topologyTreeView.getTreeItemLevel(newValue) == META_PROPERTY_COLUMN_OR_EDGE_LABEL_INDEX ||
                                this.topologyTreeView.getTreeItemLevel(newValue) == META_PROPERTY_COLUMN_INDEX) {

                            MetaTopology metaTopology = (MetaTopology) newValue.getValue();

                            if (metaTopology.getParent() instanceof VertexLabelUI vertexLabelUI) {
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
                                    view = new Pane();
                                } else {
                                    throw new IllegalStateException(String.format("Unexpected metaTopology '%s'", metaTopology.getName()));
                                }
                            } else if (metaTopology.getParent() instanceof EdgeLabelUI edgeLabelUI) {
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
                                    view = new Pane();
                                } else {
                                    throw new IllegalStateException(String.format("Unexpected metaTopology '%s'", metaTopology.getName()));
                                }
                            } else {
                                throw new IllegalStateException("Expected VertexLabelUI or EdgeLabelUI, instead got " + newValue.getValue().getClass().getSimpleName());
                            }
                        } else if (this.topologyTreeView.getTreeItemLevel(newValue) == PROPERTY_COLUMN_OR_EDGE_LABEL_INDEX) {
                            if (newValue.getValue() instanceof PropertyColumnUI propertyColumnUI) {
                                PropertyColumnFormController propertyColumnFormController = new PropertyColumnFormController(this, propertyColumnUI);
                                view = propertyColumnFormController.getView();
                            } else if (newValue.getValue() instanceof IndexUI indexUI) {
                                IndexNameFormController indexFormController = new IndexNameFormController(this, indexUI);
                                view = indexFormController.getView();
                            } else if (newValue.getValue() instanceof EdgeRoleUI) {
                                view = new Pane();
                            } else {
                                throw new IllegalStateException("not handled");
                            }
                        } else if (this.topologyTreeView.getTreeItemLevel(newValue) == PROPERTY_COLUMN_INDEX) {
                            if (newValue.getValue() instanceof PropertyColumnUI propertyColumnUI) {
                                PropertyColumnFormController propertyColumnFormController = new PropertyColumnFormController(this, propertyColumnUI);
                                view = propertyColumnFormController.getView();
                            } else if (newValue.getValue() instanceof IndexUI indexUI) {
                                IndexNameFormController indexFormController = new IndexNameFormController(this, indexUI);
                                view = indexFormController.getView();
                            } else {
                                throw new IllegalStateException("not handled");
                            }
                        }
                    }
                    if (view != null) {
                        this.rightAnchorPane.getChildren().clear();
                        AnchorPane.setTopAnchor(view, 0D);
                        AnchorPane.setRightAnchor(view, 0D);
                        AnchorPane.setBottomAnchor(view, 0D);
                        AnchorPane.setLeftAnchor(view, 0D);
                        this.rightAnchorPane.getChildren().add(view);
                    }
                }
        );
    }

    public PrimaryController2 getPrimaryController() {
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
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    if (abstractLabel instanceof VertexLabel vertexLabel) {
                        TreeItem<ISqlgTopologyUI> metaTopologyVertexLabelsTreeItem = search(schemaTreeItem, TopologyTreeItem.VERTEX_LABELS);
                        if (metaTopologyVertexLabelsTreeItem != null) {
                            TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(metaTopologyVertexLabelsTreeItem, vertexLabel.getName());
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
                            TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(metaTopologyEdgeLabelsTreeItem, abstractLabel.getName());
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

    public void addVertexLabel(GraphGroup graphGroup, GraphConfiguration graphConfiguration, VertexLabel vertexLabel) {
        TreeItem<ISqlgTopologyUI> graphGroupTreeItem = search(this.topologyTreeView.getRoot(), graphGroup.getName());
        if (graphGroupTreeItem != null) {
            TreeItem<ISqlgTopologyUI> graphConfigurationTreeItem = search(graphGroupTreeItem, graphConfiguration.getName());
            if (graphConfigurationTreeItem != null) {
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, vertexLabel.getSchema().getName());
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
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    boolean success = schemaTreeItem.getParent().getChildren().remove(schemaTreeItem);
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
                                boolean success = metaTopologyPropertyColumns.getChildren().add(new TopologyTreeItem(propertyColumnUI));
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
                                    propertyColumnUI.selectInTree(propertyColumn.getName());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void refreshEdgeLabel(GraphGroup graphGroup, GraphConfiguration graphConfiguration, Schema schema, VertexLabel vertexLabel, EdgeLabel oldEdgeLabel, EdgeLabel edgeLabel) {
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
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    SchemaUI schemaUI = (SchemaUI) schemaTreeItem.getValue();
                    TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(schemaTreeItem, vertexLabel.getName());
                    if (vertexLabelTreeItem != null) {
                        boolean success = vertexLabelTreeItem.getParent().getChildren().remove(vertexLabelTreeItem);
                        LOGGER.info("deleteVertexLabel from tree {}/{}/{} with success = {}", graphGroup.getName(), schema.getName(), vertexLabel.getName(), success);
                        VertexLabelUI vertexLabelUI = (VertexLabelUI) vertexLabelTreeItem.getValue();
                        success = schemaUI.getVertexLabelUIs().remove(vertexLabelUI);
                        LOGGER.info("deleteVertexLabel from table {}/{}/{} with success = {}", graphGroup.getName(), schema.getName(), vertexLabel.getName(), success);
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
                        TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(metaTopologyEdgeLabelsTreeItem, edgeLabel.getName());
                        if (edgeLabelTreeItem != null) {
                            EdgeLabelUI edgeLabelUI = (EdgeLabelUI) edgeLabelTreeItem.getValue();
                            boolean success = schemaUI.getEdgeLabelUIS().remove(edgeLabelUI);
                            LOGGER.debug("deleteEdgeLabel[out] from ui model with {}/{}/{}/{} with {}", graphGroup.getName(), graphConfiguration.getName(), schema.getName(), edgeLabel.getName(), success);
                            success = edgeLabelTreeItem.getParent().getChildren().remove(edgeLabelTreeItem);
                            LOGGER.debug("deleteEdgeLabel[out] from tree {}/{}/{}/{} with {}", graphGroup.getName(), graphConfiguration.getName(), schema.getName(), edgeLabel.getName(), success);
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
                TreeItem<ISqlgTopologyUI> schemaTreeItem = search(graphConfigurationTreeItem, schema.getName());
                if (schemaTreeItem != null) {
                    if (edgeLabel != null) {
                        TreeItem<ISqlgTopologyUI> edgeLabelTreeItem = search(schemaTreeItem, edgeLabel.getName());
                        if (edgeLabelTreeItem != null) {
                            TreeItem<ISqlgTopologyUI> propertyColumnTreeItem = search(edgeLabelTreeItem, propertyColumn.getName());
                            if (propertyColumnTreeItem != null) {
                                PropertyColumnUI propertyColumnUI = (PropertyColumnUI) propertyColumnTreeItem.getValue();
                                propertyColumnUI.getEdgeLabelUI().getPropertyColumnUIs().remove(propertyColumnUI);
                                propertyColumnTreeItem.getParent().getChildren().remove(propertyColumnTreeItem);
                            }
                        }
                    } else {
                        TreeItem<ISqlgTopologyUI> vertexLabelTreeItem = search(schemaTreeItem, vertexLabel.getName());
                        if (vertexLabelTreeItem != null) {
                            TreeItem<ISqlgTopologyUI> metaPropertyColumnsTreeItem = search(vertexLabelTreeItem, TopologyTreeItem.PROPERTY_COLUMNS);
                            if (metaPropertyColumnsTreeItem != null) {
                                //we do not use the regular search here as the PropertyColumnUI's name has already been changed but the edit.
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

    public void deleteIndex(GraphGroup graphGroup, GraphConfiguration graphConfiguration, Schema schema, VertexLabel vertexLabel, EdgeLabel edgeLabel, Index index) {
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
                                    indexTreeItem.getParent().getChildren().remove(indexTreeItem);
                                }
                            }
                        } else {
                            TreeItem<ISqlgTopologyUI> indexTreeItem = search(vertexLabelTreeItem, index.getName());
                            if (indexTreeItem != null) {
                                indexTreeItem.getParent().getChildren().remove(indexTreeItem);
                            }
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
            if (currentNode.getValue().getName().equals(value)) {
                return currentNode;
            } else {
                ISqlgTopologyUI sqlgTreeData = currentNode.getValue();
                if (!(sqlgTreeData instanceof GraphConfiguration) ||
                        (sqlgTreeData instanceof GraphConfiguration graphConfiguration) && graphConfiguration.isOpen()) {
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
                    } else if (treeItem instanceof GraphConfigurationTreeItem graphConfigurationTreeItem) {
                        GraphConfiguration graphConfiguration = (GraphConfiguration) graphConfigurationTreeItem.getValue();

                        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
                        Button close = new Button();
                        close.disableProperty().bind(Bindings.createBooleanBinding(() -> !graphConfiguration.isOpen(), graphConfiguration.sqlgGraphOpenPropertyProperty()));
                        Glyph closeGlyph = fontAwesome.create(FontAwesome.Glyph.CLOSE).size(12);
                        close.setGraphic(closeGlyph);
                        Button refresh = new Button();
                        refresh.disableProperty().bind(Bindings.createBooleanBinding(() -> !graphConfiguration.isOpen(), graphConfiguration.sqlgGraphOpenPropertyProperty()));
                        Glyph refreshGlyph = fontAwesome.create(FontAwesome.Glyph.REFRESH).size(12);
                        refresh.setGraphic(refreshGlyph);
                        HBox hBox = new HBox();
                        hBox.setSpacing(2);
                        Insets insets = new Insets(2, 6, 2, 6);
                        refresh.setPadding(insets);
                        close.setPadding(insets);
                        hBox.getChildren().addAll(close, refresh);
                        setGraphic(hBox);
                        setText(getItem().getName());
                        close.setOnAction(event -> {
                            graphConfiguration.closeSqlgGraph();
                            graphConfigurationTreeItem.closeGraph();
                            graphConfigurationTreeItem.setExpanded(false);
                        });
                        refresh.setOnAction(event -> {
                            graphConfiguration.refreshSqlgGraph();
                            graphConfigurationTreeItem.refreshGraph();
                            graphConfigurationTreeItem.setExpanded(false);
                        });
                    } else {
                        setText(getItem().getName());
                        setGraphic(getTreeItem().getGraphic());
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

}
