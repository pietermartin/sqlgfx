package org.sqlg.ui.controller;

import org.sqlg.ui.model.ISqlgTopologyUI;

public abstract class AbstractLabelControllerName extends BaseNameFormController {

    public AbstractLabelControllerName(LeftPaneController leftPaneController, ISqlgTopologyUI sqlgTopologyUI) {
        super(leftPaneController, sqlgTopologyUI);
    }

    protected void cancelPropertyColumns() {
    }

    protected void saveIndexes() {
    }

    protected void cancelIndexes() {
    }

}
