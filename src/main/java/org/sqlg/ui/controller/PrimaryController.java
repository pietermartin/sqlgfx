package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.Fontawesome;
import org.sqlg.ui.TopologyTreeItem;
import org.sqlg.ui.log4j2.LogListener;
import org.sqlg.ui.model.*;
import org.sqlg.ui.util.MemoryUtil;
import org.umlg.sqlg.structure.TopologyListener;
import org.umlg.sqlg.structure.topology.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PrimaryController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrimaryController.class);
    private final Root root;
    private BorderPane borderPane;
    private TabPane tabPane;
    private LeftPaneController leftPaneController;
    private User user;
    private final ObservableList<GraphGroup> graphGroups = FXCollections.observableArrayList(new ArrayList<>());
    private ScheduledExecutorService scheduledExecutorService;

    public PrimaryController(Stage stage, Root root) {
        super(stage);
        this.root = root;
    }

    public void stop() {
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.close();
        }
    }

    public Parent initialize() {
        this.borderPane = new BorderPane();
        this.borderPane.setPrefHeight(1000D);
        this.borderPane.setPrefWidth(1500D);
        StatusBar statusbar = new StatusBar();
        this.borderPane.setBottom(statusbar);
        LoginFormController loginFormController = new LoginFormController(
                this.stage,
                this.root,
                this.borderPane,
                this
        );
        loginFormController.initialise();
        return borderPane;
    }

    public GraphGroup addDefaultGraphGroup() {
        Optional<GraphGroup> graphGroupDefaultOpt = this.graphGroups.stream().filter(g -> g.getName().equals("default")).findAny();
        GraphGroup graphGroup = new GraphGroup(this.user, graphGroupDefaultOpt.isEmpty() ? "default" : "default1");
        this.user.getGraphGroups().add(graphGroup);
        this.graphGroups.add(graphGroup);
        return graphGroup;
    }

    public void log(User user) {
        //this will throw an exception is the decryption fails.
        user.decryptPasswords();

        this.user = user;
        this.borderPane.getChildren().clear();
        ToolBar toolbar = new ToolBar();
        toolbar.setMinHeight(40D);
        this.borderPane.setTop(toolbar);

        SplitPane topBottomSplitPane = new SplitPane();
        topBottomSplitPane.setDividerPosition(0, 0.85D);
        topBottomSplitPane.setOrientation(Orientation.VERTICAL);
        this.borderPane.setCenter(topBottomSplitPane);

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0, 0.25D);
        BorderPane borderPaneLeft = new BorderPane();
        AnchorPane anchorPaneRight = new AnchorPane();
        anchorPaneRight.setPadding(Insets.EMPTY);

        this.tabPane = new TabPane();
        Tab viewTab = new Tab("View", null);
        viewTab.setGraphic(Fontawesome.GEAR.label(Fontawesome.Type.Solid));
        viewTab.setClosable(false);
        this.tabPane.getTabs().add(viewTab);

        AnchorPane.setTopAnchor(this.tabPane, 0D);
        AnchorPane.setRightAnchor(this.tabPane, 0D);
        AnchorPane.setBottomAnchor(this.tabPane, 0D);
        AnchorPane.setLeftAnchor(this.tabPane, 0D);
        anchorPaneRight.getChildren().add(this.tabPane);

        VBox logVBox = new VBox();
        topBottomSplitPane.getItems().addAll(splitPane, logVBox);
        LogController logController = new LogController(logVBox);
        LogListener.INSTANCE.setLogController(logController);

        splitPane.getItems().addAll(borderPaneLeft, anchorPaneRight);
        StatusBar statusbar = new StatusBar();

        Label memoryLabel = new Label("-");
        HBox memoryBox = new HBox(memoryLabel);
        HBox.setMargin(memoryLabel, new Insets(0, 0, 0, 5));
        StackPane rootStackPane = new StackPane(memoryBox);
        rootStackPane.setMinWidth(300);
        rootStackPane.setMaxWidth(300);
        rootStackPane.setVisible(true);
        rootStackPane.setStyle("-fx-background-color:#F1F7FF");
        StackPane leftstackPane = new StackPane();
        leftstackPane.setStyle("-fx-background-color:#55555550");
        leftstackPane.setVisible(true);
        rootStackPane.getChildren().addAll(leftstackPane);
        statusbar.getRightItems().addAll(rootStackPane);
        StackPane.setAlignment(leftstackPane, Pos.CENTER_LEFT);
        this.borderPane.setBottom(statusbar);

        this.scheduledExecutorService = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
        Runnable scheduledRunnable = () -> {
            long maxMemory = Runtime.getRuntime().maxMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;
            String humanMaxMemory = MemoryUtil.humanReadableByteCountBin(maxMemory);
            String humanTotalMemory = MemoryUtil.humanReadableByteCountBin(totalMemory);
            String humanUsedMemory = MemoryUtil.humanReadableByteCountBin(usedMemory);
            double usedMemoryDouble = Long.valueOf(usedMemory).doubleValue();
            double totalMemoryDouble = Long.valueOf(totalMemory).doubleValue();
            double maxMemoryDouble = Long.valueOf(maxMemory).doubleValue();
            double width = (usedMemoryDouble / maxMemoryDouble) * memoryLabel.getWidth();
            leftstackPane.setMaxWidth(width);
            Platform.runLater(() -> {
                memoryLabel.setText(STR."used: \{humanUsedMemory}, total: \{humanTotalMemory}, max: \{humanMaxMemory}");
            });
        };
        this.scheduledExecutorService.scheduleAtFixedRate(scheduledRunnable, 1, 1, TimeUnit.SECONDS);

        this.graphGroups.addAll(this.user.getGraphGroups());

        BreadCrumbBar<ISqlgTopologyUI> breadCrumbBar = new BreadCrumbBar<>();
        breadCrumbBar.setCrumbFactory(crumb -> {
            BreadCrumbBar.BreadCrumbButton button = new BreadCrumbBar.BreadCrumbButton(crumb.getValue() != null ? crumb.getValue().getName() : "");
            Node graphic = TopologyTreeItem.graphicForTreeItem(crumb.getValue());
            if (graphic == null) {
                System.out.println(crumb.getValue().toString() + " is null");
            } else {
                button.setGraphic(graphic);
            }
            return button;
        });
        toolbar.getItems().add(breadCrumbBar);

        this.leftPaneController = new LeftPaneController(
                this,
                breadCrumbBar,
                this.graphGroups,
                borderPaneLeft,
                viewTab
        );
        this.leftPaneController.initialize();

    }


    public Stage getStage() {
        return stage;
    }

    public LeftPaneController getLeftPaneController() {
        return leftPaneController;
    }

    public void close() {
        for (GraphGroup graphGroup : this.graphGroups) {
            for (GraphConfiguration graphConfiguration : graphGroup.getGraphConfigurations()) {
                graphConfiguration.closeSqlgGraph();
            }
        }
    }

    public TopologyListener listen(GraphConfiguration graphConfiguration) {
        GraphGroup graphGroup = graphConfiguration.getGraphGroup();
        return (topologyInf, oldValue, action, beforeCommit) -> {
            LOGGER.info("topology listener {} {}, {}, {}", (beforeCommit ? "beforeCommit" : "afterCommit"), action.name(), topologyInf != null ? topologyInf.getClass().getSimpleName() : "-", oldValue != null ? oldValue.getClass().getSimpleName() : "-");
            if (!beforeCommit) {
                graphConfiguration.refreshTopologyPropertyProperty().set(true);
                leftPaneController.refreshTree();
            } else {
                switch (action) {
                    case CREATE -> {
                        if (topologyInf instanceof Schema schema) {
                            //noop
                        } else if (topologyInf instanceof VertexLabel vertexLabel) {
                            Platform.runLater(() -> {
                                LOGGER.debug("VertexLabel creation: {}/{}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), vertexLabel.getSchema().getName(), vertexLabel.getName());
                                leftPaneController.addVertexLabel(
                                        graphGroup,
                                        graphConfiguration,
                                        vertexLabel.getSchema(),
                                        vertexLabel
                                );
                            });
                        } else if (topologyInf instanceof EdgeRole edgeRole) {
                            Platform.runLater(() -> {
                                LOGGER.debug("EdgeRole creation: {}/{}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), edgeRole.getEdgeLabel().getSchema().getName(), edgeRole.getName());
                                leftPaneController.addEdgeRole(
                                        graphGroup,
                                        graphConfiguration,
                                        edgeRole
                                );
                            });
                        } else if (topologyInf instanceof EdgeLabel edgeLabel) {
                            Platform.runLater(() -> {
                                LOGGER.debug("EdgeLabel creation: {}/{}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), edgeLabel.getSchema().getName(), edgeLabel.getName());
                                leftPaneController.addEdgeLabel(
                                        graphGroup,
                                        graphConfiguration,
                                        edgeLabel
                                );
                            });
                        } else if (topologyInf instanceof PropertyColumn propertyColumn) {
                            Platform.runLater(() -> {
                                LOGGER.debug("PropertyColumn creation: {}/{}/{}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), propertyColumn.getParentLabel().getSchema().getName(), propertyColumn.getParentLabel().getName(), propertyColumn.getName());
                                AbstractLabel abstractLabel = propertyColumn.getParentLabel();
                                Schema schema = abstractLabel.getSchema();
                                leftPaneController.addPropertyColumn(
                                        graphGroup,
                                        graphConfiguration,
                                        schema,
                                        abstractLabel,
                                        propertyColumn
                                );
                            });
                        } else if (topologyInf instanceof Index ignore) {

                        }
                    }
                    case ADD_IN_VERTEX_LABEL_TO_EDGE -> {
                    }
                    case DELETE -> {
                        if (topologyInf instanceof Schema schema) {
                            Platform.runLater(() -> {
                                LOGGER.debug("Schema deletion: {}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), schema.getName());
                                leftPaneController.deleteSchema(
                                        graphGroup,
                                        graphConfiguration,
                                        schema
                                );
                            });
                        } else if (oldValue instanceof VertexLabel oldVertexLabel) {
                            Platform.runLater(() -> {
                                LOGGER.debug("VertexLabel deletion: {}/{}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), oldVertexLabel.getSchema().getName(), oldVertexLabel.getName());
                                leftPaneController.deleteVertexLabel(
                                        graphGroup,
                                        graphConfiguration,
                                        oldVertexLabel.getSchema(),
                                        oldVertexLabel
                                );
                            });
                        } else if (oldValue instanceof EdgeRole edgeRole) {
                            Schema schema = edgeRole.getEdgeLabel().getSchema();
                            Platform.runLater(() -> {
                                LOGGER.debug("EdgeRole deletion : {}/{}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), schema.getName(), edgeRole.getName());
                                leftPaneController.deleteEdgeRole(
                                        graphGroup,
                                        graphConfiguration,
                                        schema,
                                        edgeRole,
                                        edgeRole.getDirection()
                                );
                            });
                        } else if (oldValue instanceof EdgeLabel edgeLabel) {
                            Schema schema = edgeLabel.getSchema();
                            Platform.runLater(() -> {
                                LOGGER.debug("EdgeLabel deletion: {}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), edgeLabel.getName());
                                for (EdgeRole outEdgeRole : edgeLabel.getOutEdgeRoles()) {
                                    leftPaneController.deleteEdgeRole(
                                            graphGroup,
                                            graphConfiguration,
                                            schema,
                                            outEdgeRole,
                                            Direction.OUT
                                    );
                                }
                                for (EdgeRole inEdgeRole : edgeLabel.getInEdgeRoles()) {
                                    Schema inEdgeRoleSchema = inEdgeRole.getVertexLabel().getSchema();
                                    leftPaneController.deleteEdgeRole(
                                            graphGroup,
                                            graphConfiguration,
                                            inEdgeRoleSchema,
                                            inEdgeRole,
                                            Direction.IN
                                    );
                                }
                                leftPaneController.deleteEdgeLabel(
                                        graphGroup,
                                        graphConfiguration,
                                        edgeLabel.getSchema(),
                                        edgeLabel
                                );
                            });
                        } else if (oldValue instanceof PropertyColumn propertyColumnToDelete) {
                            Platform.runLater(() -> {
                                LOGGER.debug("PropertyColumn deletion: {}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), propertyColumnToDelete.getName());
                                AbstractLabel abstractLabel = propertyColumnToDelete.getParentLabel();
                                if (abstractLabel instanceof VertexLabel) {
                                    leftPaneController.deletePropertyColumn(
                                            graphGroup,
                                            graphConfiguration,
                                            abstractLabel.getSchema(),
                                            (VertexLabel) abstractLabel,
                                            null,
                                            propertyColumnToDelete
                                    );
                                } else {
                                    EdgeLabel edgeLabel = (EdgeLabel) abstractLabel;
                                    for (VertexLabel outVertexLabel : edgeLabel.getOutVertexLabels()) {
                                        leftPaneController.deletePropertyColumn(
                                                graphGroup,
                                                graphConfiguration,
                                                abstractLabel.getSchema(),
                                                outVertexLabel,
                                                (EdgeLabel) abstractLabel,
                                                propertyColumnToDelete
                                        );
                                    }
                                }
                            });
                        } else if (topologyInf instanceof Index index) {
                            Platform.runLater(() -> {
                                AbstractLabel abstractLabel = index.getParentLabel();
                                LOGGER.debug("Index deletion: {}/{}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), abstractLabel.getName(), index.getName());
                                if (abstractLabel instanceof VertexLabel) {
                                    leftPaneController.deleteIndex(
                                            graphGroup,
                                            graphConfiguration,
                                            abstractLabel.getSchema(),
                                            (VertexLabel) abstractLabel,
                                            null,
                                            index
                                    );
                                } else {
                                    EdgeLabel edgeLabel = (EdgeLabel) abstractLabel;
                                    for (VertexLabel outVertexLabel : edgeLabel.getOutVertexLabels()) {
                                        leftPaneController.deleteIndex(
                                                graphGroup,
                                                graphConfiguration,
                                                abstractLabel.getSchema(),
                                                outVertexLabel,
                                                (EdgeLabel) abstractLabel,
                                                index
                                        );
                                    }
                                }
                            });
                        } else if (topologyInf instanceof Partition partition) {
                            Platform.runLater(() -> {
                                AbstractLabel abstractLabel = partition.getAbstractLabel();
                                LOGGER.debug("Partition deletion: {}/{}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), abstractLabel.getName(), partition.getName());
                                if (abstractLabel instanceof VertexLabel) {
                                    leftPaneController.deletePartition(
                                            graphGroup,
                                            graphConfiguration,
                                            abstractLabel.getSchema(),
                                            (VertexLabel) abstractLabel,
                                            null,
                                            partition
                                    );
                                } else {
                                    EdgeLabel edgeLabel = (EdgeLabel) abstractLabel;
                                    for (VertexLabel outVertexLabel : edgeLabel.getOutVertexLabels()) {
                                        leftPaneController.deletePartition(
                                                graphGroup,
                                                graphConfiguration,
                                                abstractLabel.getSchema(),
                                                outVertexLabel,
                                                (EdgeLabel) abstractLabel,
                                                partition
                                        );
                                    }
                                }
                            });
                        }
                    }
                    case UPDATE -> {
                        if (topologyInf instanceof Schema schema) {
                            Platform.runLater(() -> {
                                LOGGER.debug("Schema update : {}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), schema.getName());
                            });
                        } else if (topologyInf instanceof VertexLabel vertexLabel) {
                            if (!vertexLabel.getName().equals(oldValue.getName())) {
                                Platform.runLater(() -> {
                                    LOGGER.debug("VertexLabel update: {}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), vertexLabel.getName());
                                });
                            }
                        } else if (topologyInf instanceof EdgeLabel edgeLabel) {
                            Platform.runLater(() -> {
                                LOGGER.debug("EdgeLabel update: {}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), edgeLabel.getName());
                                for (VertexLabel outVertexLabel : edgeLabel.getOutVertexLabels()) {
                                    leftPaneController.refreshEdgeLabel(
                                            graphGroup,
                                            graphConfiguration,
                                            edgeLabel.getSchema(),
                                            outVertexLabel,
                                            (EdgeLabel) oldValue,
                                            edgeLabel
                                    );
                                }
                                for (VertexLabel inVertexLabel : edgeLabel.getInVertexLabels()) {
                                    leftPaneController.refreshEdgeLabel(
                                            graphGroup,
                                            graphConfiguration,
                                            edgeLabel.getSchema(),
                                            inVertexLabel,
                                            (EdgeLabel) oldValue,
                                            edgeLabel
                                    );
                                }
                            });
                        } else if (topologyInf instanceof PropertyColumn propertyColumn) {
                            Platform.runLater(() -> {
                                LOGGER.debug("PropertyColumn update: {}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), propertyColumn.getName());
                                leftPaneController.removeOldAddUpdatedPropertyColumn(
                                        beforeCommit,
                                        graphGroup,
                                        graphConfiguration,
                                        propertyColumn.getParentLabel().getSchema(),
                                        propertyColumn.getParentLabel(),
                                        (PropertyColumn) oldValue,
                                        propertyColumn
                                );
                            });
                        } else if (topologyInf instanceof Index index) {
                            Platform.runLater(() -> {
                                AbstractLabel abstractLabel = index.getParentLabel();
                                LOGGER.debug("Index update: {}/{}/{}/{}", graphGroup.getName(), graphConfiguration.getName(), abstractLabel.getName(), index.getName());
                            });
                        }
                    }
                }

            }
        };

    }

    public void alert(String heading, String message, Exception e) {
        showDialog(Alert.AlertType.ERROR, heading, message, e, (ignore) -> {
        });
    }

    public void addGremlinTab(GraphConfiguration graphConfiguration) {
        if (graphConfiguration.isOpen()) {
            new GremlinQueryTab(this.tabPane, graphConfiguration);
        } else {
            showDialog(Alert.AlertType.INFORMATION, graphConfiguration.getName(), "The graph needs to be opened to query.");
        }
    }

}
