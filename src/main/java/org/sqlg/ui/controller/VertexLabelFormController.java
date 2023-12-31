package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.*;
import org.umlg.sqlg.structure.SqlgGraph;
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
        this.vertexLabelUI.getSchemaUI().getVertexLabelUIs().remove(this.vertexLabelUI);
        this.vertexLabelUI.getVertexLabel().remove();
    }

    @Override
    protected void cancelName() {
        this.vertexLabelUI.refresh();
        this.sqlgTreeDataFormNameTxt.setText(this.vertexLabelUI.getName());
    }

    @Override
    protected void rename() {
        if (!this.vertexLabelUI.getVertexLabel().getName().equals(this.sqlgTreeDataFormNameTxt.getText())) {
            SqlgGraph sqlgGraph = getSqlgGraph();
            try {
                VertexLabel vertexLabel = this.vertexLabelUI.getVertexLabel();
                vertexLabel.rename(this.sqlgTreeDataFormNameTxt.getText());
                sqlgGraph.tx().commit();
                Platform.runLater(() -> {
                    SchemaUI schemaUI = this.vertexLabelUI.getSchemaUI();
                    GraphConfiguration graphConfiguration = schemaUI.getGraphConfiguration();
                    GraphGroup graphGroup = graphConfiguration.getGraphGroup();
                    leftPaneController.selectVertexLabel(
                            graphGroup,
                            graphConfiguration,
                            schemaUI.getSchema(),
                            this.sqlgTreeDataFormNameTxt.getText()
                    );
                });
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


        GridPane partitionGridPane = new GridPane();
        partitionGridPane.setHgap(100);
        partitionGridPane.setVgap(5);
        partitionGridPane.setPadding(new Insets(5, 5, 5, 5));
        int rowIndex = 0;

        Label partitionTypeLabel = new Label("partitionType");
        GridPane.setConstraints(partitionTypeLabel, 0, rowIndex);
        TextField partitionTypeTextField = new TextField(vertexLabelUI.getPartitionType());
        partitionTypeTextField.setEditable(false);
        partitionTypeTextField.setDisable(true);
        GridPane.setConstraints(partitionTypeTextField, 1, rowIndex);
        rowIndex++;

        Label partitionExpresssionLabel = new Label("partitionExpression");
        GridPane.setConstraints(partitionExpresssionLabel, 0, rowIndex);
        TextField partitionExpressionTextField = new TextField(vertexLabelUI.getPartitionExpression());
        partitionExpressionTextField.setEditable(false);
        partitionExpressionTextField.setDisable(true);
        GridPane.setConstraints(partitionExpressionTextField, 1, rowIndex);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(80);
        column2.setHgrow(Priority.ALWAYS);
        partitionGridPane.getColumnConstraints().addAll(column1, column2); // each get 50% of width
        partitionGridPane.getChildren().addAll(partitionTypeLabel, partitionTypeTextField, partitionExpresssionLabel, partitionExpressionTextField);

        Node propertiesTableView = ControllerUtil.propertyColumnsTableView(
                vertexLabelUI.getPropertyColumnUIs(),
                this.editToggleSwitch.selectedProperty(),
                ignore -> ControllerUtil.savePropertyColumns(
                        getSqlgGraph(),
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
                ignore -> cancelPropertyColumns()
        );
        VBox.setVgrow(propertiesTableView, Priority.ALWAYS);
        Node indexesTableView = ControllerUtil.indexesTableView(
                vertexLabelUI.getIndexUIs(),
                this.editToggleSwitch.selectedProperty(),
                ignore -> saveIndexes(),
                ignore -> cancelIndexes()
        );
        VBox.setVgrow(indexesTableView, Priority.ALWAYS);
        return List.of(partitionGridPane, propertiesTableView, indexesTableView);
    }

    @Override
    protected void cancelPropertyColumns() {
        System.out.println("cancelPropertyColumns");
    }

}
