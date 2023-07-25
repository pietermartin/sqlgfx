package org.sqlg.ui.controller;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.*;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.Schema;
import org.umlg.sqlg.structure.topology.VertexLabel;

import java.util.Collection;
import java.util.List;

public class VertexLabelFormController extends AbstractLabelControllerName {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertexLabelFormController.class);
    private final VertexLabelUI vertexLabelUI;

    public VertexLabelFormController(LeftPaneController leftPaneController, VertexLabelUI vertexLabelUI) {
        super(leftPaneController, vertexLabelUI);
        this.vertexLabelUI = vertexLabelUI;
    }

    @Override
    protected SqlgGraph getSqlgGraph() {
        return vertexLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
    }

    @Override
    protected void delete() {
        SchemaUI schemaUI = this.vertexLabelUI.getSchemaUI();
        GraphConfiguration graphConfiguration = schemaUI.getGraphConfiguration();
        GraphGroup graphGroup = graphConfiguration.getGraphGroup();
        this.vertexLabelUI.getSchemaUI().getVertexLabelUIs().remove(this.vertexLabelUI);
        this.vertexLabelUI.getVertexLabel().remove();
        this.leftPaneController.deleteVertexLabel(graphGroup, graphConfiguration, schemaUI.getSchema(), this.vertexLabelUI.getVertexLabel());
    }

    @Override
    protected void rename() {
        if (!this.vertexLabelUI.getVertexLabel().getName().equals(this.sqlgTreeDataFormNameTxt.getText())) {
            SqlgGraph sqlgGraph = getSqlgGraph();
            try {
                VertexLabel vertexLabel = this.vertexLabelUI.getVertexLabel();
                Schema schema = vertexLabel.getSchema();
                this.vertexLabelUI.getVertexLabel().rename(this.sqlgTreeDataFormNameTxt.getText());
                sqlgGraph.tx().commit();
                VertexLabel renamedVertexLabel = schema.getVertexLabel(this.sqlgTreeDataFormNameTxt.getText()).orElseThrow();
                this.vertexLabelUI.setVertexLabel(renamedVertexLabel);
                this.leftPaneController.refreshTree();
                showDialog(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Renamed VertexLabel to '" + this.sqlgTreeDataFormNameTxt.getText() + "'"
                );
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                showDialog(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to renamed VertexLabel to '" + this.sqlgTreeDataFormNameTxt.getText() + "'",
                        e,
                        result -> this.sqlgTreeDataFormNameTxt.setText(this.vertexLabelUI.getVertexLabel().getName())
                );
            } finally {
                sqlgGraph.tx().rollback();
            }
        }
    }

    @Override
    protected Collection<Node> additionalChildren(ISqlgTopologyUI sqlgTopologyUI) {
        VertexLabelUI vertexLabelUI = (VertexLabelUI) sqlgTopologyUI;
        Node propertiesTableView = ControllerUtil.propertyColumnsTableView(
                vertexLabelUI.getPropertyColumnUIs(),
                this.editToggleSwitch.selectedProperty(),
                event -> ControllerUtil.savePropertyColumns(
                        getSqlgGraph(),
                        vertexLabelUI.getVertexLabel(),
                        vertexLabelUI.getPropertyColumnUIs(),
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
                                    result -> this.sqlgTreeDataFormNameTxt.setText(vertexLabelUI.getVertexLabel().getName())
                            );
                            return null;
                        }
                ),
                event -> cancelPropertyColumns()
        );
        VBox.setVgrow(propertiesTableView, Priority.ALWAYS);
        Node indexesTableView = ControllerUtil.indexesTableView(
                vertexLabelUI.getIndexUIs(),
                this.editToggleSwitch.selectedProperty(),
                event -> saveIndexes(),
                event -> cancelIndexes()
        );
        VBox.setVgrow(indexesTableView, Priority.ALWAYS);
        return List.of(propertiesTableView, indexesTableView);
    }


}
