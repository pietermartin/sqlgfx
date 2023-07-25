package org.sqlg.ui.controller;

import javafx.scene.Node;
import org.sqlg.ui.model.ISqlgTopologyUI;
import org.sqlg.ui.model.SchemaUI;
import org.umlg.sqlg.structure.SqlgGraph;

import java.util.Collection;
import java.util.List;

public class SchemaFormController extends BaseNameFormController {

    private final SchemaUI schemaUI;

    public SchemaFormController(LeftPaneController leftPaneController, SchemaUI schemaUI) {
        super(leftPaneController, schemaUI);
        this.schemaUI = schemaUI;
    }

    @Override
    protected SqlgGraph getSqlgGraph() {
        return schemaUI.getGraphConfiguration().getSqlgGraph();
    }

    @Override
    protected void rename() {

    }

    @Override
    protected Collection<Node> additionalChildren(ISqlgTopologyUI sqlgTopologyUI) {
        return List.of();
    }
}
