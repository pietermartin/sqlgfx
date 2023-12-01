package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;
import org.sqlg.ui.model.SchemaUI;
import org.sqlg.ui.model.VertexLabelUI;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.PartitionType;

import java.util.Arrays;

public class VertexLabelTableViewController extends BaseController {

    protected final ToggleSwitch editToggleSwitch;
    protected final LeftPaneController leftPaneController;
    protected final VBox root;

    public VertexLabelTableViewController(
            LeftPaneController leftPaneController,
            SchemaUI schemaUI
    ) {
        super(leftPaneController.getPrimaryController().getStage());

        this.leftPaneController = leftPaneController;
        this.root = new VBox(10);
        this.root.setPadding(Insets.EMPTY);
        this.root.setMaxHeight(Double.MAX_VALUE);

        this.editToggleSwitch = new ToggleSwitch("Edit");
        this.editToggleSwitch.setLayoutX(70);
        this.editToggleSwitch.setLayoutY(168);
        HBox editBox = new HBox();
        editBox.setPadding(new Insets(12, 5, 0, 0));
        editBox.setAlignment(Pos.CENTER_RIGHT);
        editBox.getChildren().addAll(editToggleSwitch);

        this.root.getChildren().add(editBox);

        TableView<VertexLabelUI> tableView = new TableView<>();
        tableView.editableProperty().bind(this.editToggleSwitch.selectedProperty());
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);

        TableColumn<VertexLabelUI, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<VertexLabelUI, String> partitionTypeColumn = new TableColumn<>("partitionType");
        partitionTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(Arrays.stream(PartitionType.values()).map(PartitionType::name).toList().toArray(new String[]{})));
        partitionTypeColumn.setCellValueFactory(p -> p.getValue().partitionTypeProperty());
        partitionTypeColumn.setEditable(false);

        TableColumn<VertexLabelUI, Boolean> delete = new TableColumn<>("delete");
        delete.setText("delete");
        delete.setCellValueFactory(p -> p.getValue().deleteProperty());
        delete.setCellFactory(CheckBoxTableCell.forTableColumn(delete));
        delete.setPrefWidth(60);

        tableView.getColumns().addAll(nameColumn, partitionTypeColumn, delete);

        tableView.setItems(schemaUI.getVertexLabelUIs());

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setPadding(new Insets(0, 5, 0, 0));
        Button save = new Button("Save");
        ButtonBar.setButtonData(save, ButtonBar.ButtonData.OK_DONE);
        Button cancel = new Button("Cancel");
        ButtonBar.setButtonData(cancel, ButtonBar.ButtonData.CANCEL_CLOSE);
        buttonBar.getButtons().addAll(save, cancel);
        VBox vBox = new VBox(5, tableView, buttonBar);
        vBox.setPadding(new Insets(0, 0, 5, 0));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(buttonBar, Priority.NEVER);
        VBox.setVgrow(vBox, Priority.ALWAYS);

        save.disableProperty().bind(Bindings.createBooleanBinding(() -> !this.editToggleSwitch.selectedProperty().get(), this.editToggleSwitch.selectedProperty()));
        save.setOnAction(ignore -> {
            SqlgGraph sqlgGraph = schemaUI.getGraphConfiguration().getSqlgGraph();
            try {
                for (VertexLabelUI vertexLabelUI : schemaUI.getVertexLabelUIs()) {
                    if (vertexLabelUI.isDelete()) {
                        vertexLabelUI.getVertexLabel().remove();
                    } else if (!vertexLabelUI.getName().equals(vertexLabelUI.getVertexLabel().getName())) {
                        vertexLabelUI.getVertexLabel().rename(vertexLabelUI.getName());
                    }
                }
                sqlgGraph.tx().commit();
                showDialog(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Saved VertexLabels"
                );
            } catch (Exception e) {
                showDialog(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to save VertexLabels",
                        e,
                        ignore1 -> {
                        }
                );
            } finally {
                sqlgGraph.tx().rollback();
            }
        });
        cancel.setOnAction(ignore -> {
            for (VertexLabelUI vertexLabelUI : schemaUI.getVertexLabelUIs()) {
                vertexLabelUI.reset();
            }
        });
        this.root.getChildren().add(vBox);
    }

    private void save() {

    }

    private void cancel() {

    }

    public Parent getView() {
        return this.root;
    }
}
