package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.EdgeLabelUI;
import org.sqlg.ui.model.ISqlgTopologyUI;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.EdgeLabel;

import java.util.Collection;
import java.util.List;

public class EdgeLabelFormController extends AbstractLabelControllerName {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeLabelFormController.class);
    private final EdgeLabelUI edgeLabelUI;

    public EdgeLabelFormController(LeftPaneController leftPaneController, EdgeLabelUI edgeLabelUI) {
        super(leftPaneController, edgeLabelUI);
        this.edgeLabelUI = edgeLabelUI;
    }

    @Override
    protected SqlgGraph getSqlgGraph() {
        return this.edgeLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
    }

    @Override
    protected void delete() {
        EdgeLabel edgeLabel = this.edgeLabelUI.getEdgeLabel();
        edgeLabel.remove();
    }

    @Override
    protected void rename() {
        if (!this.edgeLabelUI.getEdgeLabel().getName().equals(this.sqlgTreeDataFormNameTxt.getText())) {
            SqlgGraph sqlgGraph = getSqlgGraph();
            try {
                EdgeLabel edgeLabel = this.edgeLabelUI.getEdgeLabel();
                edgeLabel.rename(this.sqlgTreeDataFormNameTxt.getText());
                sqlgGraph.tx().commit();
                Platform.runLater(() -> this.edgeLabelUI.selectInTree(this.sqlgTreeDataFormNameTxt.getText()));
                showDialog(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Renamed EdgeLabel to '" + this.sqlgTreeDataFormNameTxt.getText() + "'"
                );
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                showDialog(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to renamed EdgeLabel to '" + this.sqlgTreeDataFormNameTxt.getText() + "'",
                        e,
                        result -> this.sqlgTreeDataFormNameTxt.setText(this.edgeLabelUI.getEdgeLabel().getName())
                );
            } finally {
                sqlgGraph.tx().rollback();
            }
        }

    }

    @Override
    protected Collection<Node> additionalChildren(ISqlgTopologyUI sqlgTopologyUI) {
        EdgeLabelUI edgeLabelUI = (EdgeLabelUI) sqlgTopologyUI;
        Node propertiesTableView = ControllerUtil.propertyColumnsTableView(
                edgeLabelUI.getPropertyColumnUIs(),
                this.editToggleSwitch.selectedProperty(),
                event -> ControllerUtil.savePropertyColumns(
                        getSqlgGraph(),
                        edgeLabelUI.getPropertyColumnUIs(),
                        result -> {
                            assert result : "expected true";
                            showDialog(
                                    Alert.AlertType.INFORMATION,
                                    "Success",
                                    "Saved PropertyColumns"
                            );
                            return null;
                        },
                        exception -> {
                            showDialog(
                                    Alert.AlertType.ERROR,
                                    "Error",
                                    "Failed to save PropertyColumns",
                                    exception,
                                    result -> this.sqlgTreeDataFormNameTxt.setText(edgeLabelUI.getEdgeLabel().getName())
                            );
                            return null;
                        }
                ),
                event -> cancelPropertyColumns()
        );
        VBox.setVgrow(propertiesTableView, Priority.ALWAYS);
        return List.of(propertiesTableView);
    }


}
