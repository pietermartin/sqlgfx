package org.sqlg.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.controller.LeftPaneController;
import org.sqlg.ui.model.GraphConfiguration;
import org.sqlg.ui.model.ISqlgTopologyUI;
import org.sqlg.ui.model.MetaTopology;
import org.sqlg.ui.model.SchemaUI;

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
                    Task<Void> task = new Task<>() {
                        @Override
                        public Void call() {
                            childrenLoadedStatus = ChildrenLoadedStatus.LOADING;
                            try {
                                graphConfiguration.openSqlgGraph();
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
                } else {
                    return super.getChildren();
                }
            }
        }
        return super.getChildren();
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
            metaSchemaTreeItems.add(metaSchema);
            ObservableList<TreeItem<ISqlgTopologyUI>> schemaTreeItems = FXCollections.observableArrayList();
            for (SchemaUI schemaUi : graphConfiguration.getSchemaUis()) {
                schemaTreeItems.add(new TopologyTreeItem(schemaUi));
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
