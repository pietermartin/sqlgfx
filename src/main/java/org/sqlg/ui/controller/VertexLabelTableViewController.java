package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.tableview2.cell.ComboBox2TableCell;
import org.sqlg.ui.model.SchemaUI;
import org.sqlg.ui.model.VertexLabelUI;
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


        TableColumn<VertexLabelUI, ?> partitionColumn = new TableColumn<>("Partition");
        TableColumn<VertexLabelUI, String> partitionTypeColumn = new TableColumn<>("partitionType");
        partitionTypeColumn.setCellFactory(ComboBox2TableCell.forTableColumn(Arrays.stream(PartitionType.values()).map(PartitionType::name).toList().toArray(new String[]{})));
        partitionTypeColumn.setCellValueFactory(p -> p.getValue().partitionTypeProperty());

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
        save.setOnAction(x -> {

        });
        cancel.setOnAction(x -> {

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
