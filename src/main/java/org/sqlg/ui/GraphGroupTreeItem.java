package org.sqlg.ui;

import javafx.scene.control.TreeItem;
import org.sqlg.ui.controller.LeftPaneController;
import org.sqlg.ui.model.ISqlgTopologyUI;

public class GraphGroupTreeItem  extends TreeItem<ISqlgTopologyUI> {

    private final LeftPaneController leftPaneController;

    public GraphGroupTreeItem(LeftPaneController leftPaneController, ISqlgTopologyUI treeItem) {
        super(treeItem);
        this.leftPaneController = leftPaneController;
    }

}
