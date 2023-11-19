package org.sqlg.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.controller.DatabasePasswordDialogController;
import org.sqlg.ui.controller.LeftPaneController;
import org.sqlg.ui.model.GraphConfiguration;
import org.sqlg.ui.model.ISqlgTopologyUI;
import org.sqlg.ui.model.MetaTopology;
import org.sqlg.ui.model.SchemaUI;

import java.util.Comparator;

public class GraphConfigurationTreeItem extends TreeItem<ISqlgTopologyUI> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphConfigurationTreeItem.class);
    private final LeftPaneController leftPaneController;

    enum ChildrenLoadedStatus {NOT_LOADED, LOADING, LOADED}

    private boolean isFirstTimeChildren = true;
    private boolean isGraphOpened = false;
    private boolean isGraphOpening = false;
    private ChildrenLoadedStatus childrenLoadedStatus = ChildrenLoadedStatus.NOT_LOADED;

    public GraphConfigurationTreeItem(LeftPaneController leftPaneController, ISqlgTopologyUI treeItem) {
        super(treeItem);
        this.leftPaneController = leftPaneController;
    }

    @Override
    public ObservableList<TreeItem<ISqlgTopologyUI>> getChildren() {
        if (this.isFirstTimeChildren) {

            if (this.isGraphOpened) {
                // First getChildren() call, so we actually go off and
                // determine the children of the File contained in this TreeItem.
                this.isFirstTimeChildren = false;
                super.getChildren().setAll(buildMetaSchemaAndSchemaTreeItems());
            } else {
                if (!this.isGraphOpening) {
                    this.isGraphOpening = true;
                    GraphConfiguration graphConfiguration = (GraphConfiguration) getValue();
                    if (graphConfiguration.getJdbcPassword() == null) {
                        Platform.runLater(() -> {
                            new DatabasePasswordDialogController(
                                    this.leftPaneController.getPrimaryController().getStage(),
                                    this
                            );
                        });
                    } else {
                        openGraphConfigurationTreeItem(graphConfiguration);
                    }
                } else {
                    return super.getChildren();
                }
            }
        }
        return super.getChildren();
    }

    public void openGraphConfigurationTreeItem(GraphConfiguration graphConfiguration) {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                childrenLoadedStatus = ChildrenLoadedStatus.LOADING;
                try {
                    graphConfiguration.openSqlgGraph(leftPaneController.getPrimaryController().listen(graphConfiguration));
                    isGraphOpened = true;
                    isGraphOpening = false;
                    isFirstTimeChildren = false;
                    childrenLoadedStatus = ChildrenLoadedStatus.LOADED;
                    getChildren().addAll(buildMetaSchemaAndSchemaTreeItems());
                } catch (Exception e) {
                    childrenLoadedStatus = ChildrenLoadedStatus.LOADED;
                    leftPaneController.refreshTree();
                    LOGGER.info("failed to open graph configuration for graph '{}'", graphConfiguration.getName(), e);
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    private ObservableList<TreeItem<ISqlgTopologyUI>> buildMetaSchemaAndSchemaTreeItems() {
        ISqlgTopologyUI value = getValue();
        if (value instanceof GraphConfiguration graphConfiguration) {
            ObservableList<TreeItem<ISqlgTopologyUI>> metaSchemaTreeItems = FXCollections.observableArrayList();
            TreeItem<ISqlgTopologyUI> metaSchema = new TreeItem<>(new MetaTopology("Schemas", graphConfiguration));
            metaSchema.setGraphic(Fontawesome.LIST_UL.label());
            metaSchemaTreeItems.add(metaSchema);
            ObservableList<TreeItem<ISqlgTopologyUI>> schemaTreeItems = FXCollections.observableArrayList();
            ObservableList<SchemaUI> schemaUIS = graphConfiguration.getSchemaUis();
            schemaUIS.sort(Comparator.comparing(SchemaUI::getName));
            for (SchemaUI schemaUi : schemaUIS) {
                TopologyTreeItem schemaTopologyTreeItem = new TopologyTreeItem(schemaUi);
                schemaTopologyTreeItem.setGraphic(Fontawesome.LIST_UL.label());
                schemaTreeItems.add(schemaTopologyTreeItem);
            }
            metaSchema.getChildren().addAll(schemaTreeItems);
            return metaSchemaTreeItems;
        }
        return FXCollections.emptyObservableList();
    }

    public boolean isLoading() {
        return childrenLoadedStatus == ChildrenLoadedStatus.LOADING;
    }

    public void refreshGraph() {
        getChildren().clear();
        this.isFirstTimeChildren = true;
    }

    public void closeGraph() {
        getChildren().clear();
        this.isGraphOpened = false;
        this.isGraphOpening = false;
        this.isFirstTimeChildren = true;
    }

}
