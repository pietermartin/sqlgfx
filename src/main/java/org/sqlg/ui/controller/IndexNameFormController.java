package org.sqlg.ui.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.sqlg.ui.model.ISqlgTopologyUI;
import org.sqlg.ui.model.IndexUI;
import org.umlg.sqlg.structure.SqlgGraph;

import java.util.Collection;
import java.util.List;

public class IndexNameFormController extends BaseNameFormController {

    private final IndexUI indexUI;

    public IndexNameFormController(LeftPaneController leftPaneController, IndexUI indexUI) {
        super(leftPaneController, indexUI);
        this.indexUI = indexUI;
        this.sqlgTreeDataFormNameTxt.disableProperty().unbind();
        this.sqlgTreeDataFormNameTxt.setDisable(true);
    }

    @Override
    protected SqlgGraph getSqlgGraph() {
        if (this.indexUI.getVertexLabelUI() != null) {
            return this.indexUI.getVertexLabelUI().getSchemaUI().getGraphConfiguration().getSqlgGraph();
        } else {
            return this.indexUI.getEdgeLabelUI().getSchemaUI().getGraphConfiguration().getSqlgGraph();
        }
    }

    @Override
    protected void rename() {

    }

    @Override
    protected void delete() {
        this.indexUI.getIndex().remove();
    }

    protected void save() {
        this.indexUI.getIndex().rename(this.sqlgTreeDataFormNameTxt.getText());
    }

    @Override
    protected Collection<Node> additionalChildren(ISqlgTopologyUI sqlgTopologyUI) {
        IndexUI indexUI = (IndexUI) sqlgTopologyUI;
        Node propertiesTableView = ControllerUtil.propertyColumnsTableView(
                indexUI.getPropertyColumnUIs(),
                BooleanProperty.booleanProperty(new SimpleBooleanProperty(false)),
                null,
                null
        );
        VBox.setVgrow(propertiesTableView, Priority.ALWAYS);
        return List.of(propertiesTableView);
    }
}
