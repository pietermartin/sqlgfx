package org.sqlg.ui.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;
import org.sqlg.ui.model.EdgeLabelUI;
import org.sqlg.ui.model.PropertyColumnUI;
import org.sqlg.ui.model.VertexLabelUI;

public class PropertyColumnTableViewController extends BaseController {

    protected final ToggleSwitch editToggleSwitch;
    protected final LeftPaneController leftPaneController;
    protected final VBox root;
    private final VertexLabelUI vertexLabelUI;
    private final EdgeLabelUI edgeLabelUI;

    public PropertyColumnTableViewController(
            LeftPaneController leftPaneController,
            VertexLabelUI vertexLabelUI,
            EdgeLabelUI edgeLabelUI
    ) {
        super(leftPaneController.getPrimaryController().getStage());
        assert vertexLabelUI == null || edgeLabelUI == null : "Either vertexLabelUI or edgeLabelUI not both expected.";
        this.vertexLabelUI = vertexLabelUI;
        this.edgeLabelUI = edgeLabelUI;

        this.leftPaneController = leftPaneController;
        this.root = new VBox(10);
        this.root.setPadding(new Insets(10, 10, 10, 10));
        this.root.setMaxHeight(Double.MAX_VALUE);

        this.editToggleSwitch = new ToggleSwitch("Edit");
        this.editToggleSwitch.setLayoutX(70);
        this.editToggleSwitch.setLayoutY(168);
        HBox editBox = new HBox();
        editBox.setPadding(new Insets(30, 30, 0, 30));
        editBox.setAlignment(Pos.CENTER_RIGHT);
        editBox.getChildren().addAll(editToggleSwitch);

        this.root.getChildren().add(editBox);
        init(vertexLabelUI, edgeLabelUI);
    }

    private void init(VertexLabelUI vertexLabelUI, EdgeLabelUI edgeLabelUI) {
        Node propertiesTableView;
        if (vertexLabelUI != null) {
            propertiesTableView = ControllerUtil.propertyColumnsTableView(
                    vertexLabelUI.getPropertyColumnUIs(),
                    editToggleSwitch.selectedProperty(),
                    event -> ControllerUtil.savePropertyColumns(
                            vertexLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph(),
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
                                        result -> cancel()
                                );
                                return null;
                            }
                    ),
                    event -> cancel()
            );
            this.root.getChildren().add(propertiesTableView);
        } else {
            propertiesTableView = ControllerUtil.propertyColumnsTableView(
                    edgeLabelUI.getPropertyColumnUIs(),
                    editToggleSwitch.selectedProperty(),
                    event -> ControllerUtil.savePropertyColumns(
                            edgeLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph(),
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
                                        result -> {}
                                );
                                return null;
                            }
                    ),
                    event -> cancel()
            );
            this.root.getChildren().add(propertiesTableView);
        }
    }

    private void cancel() {
        if (this.vertexLabelUI != null) {
            for (PropertyColumnUI propertyColumnUI : this.vertexLabelUI.getPropertyColumnUIs()) {
                propertyColumnUI.reset();
            }
            this.vertexLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph().tx().rollback();
        }
        if (this.edgeLabelUI != null) {
            for (PropertyColumnUI propertyColumnUI : this.edgeLabelUI.getPropertyColumnUIs()) {
                propertyColumnUI.reset();
            }
            this.edgeLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph().tx().rollback();
        }
    }

    public Parent getView() {
        return this.root;
    }
}
