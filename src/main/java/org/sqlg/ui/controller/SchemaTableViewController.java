package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.GraphConfiguration;
import org.sqlg.ui.model.SchemaUI;

public class SchemaTableViewController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaTableViewController.class);
    protected final ToggleSwitch editToggleSwitch;
    protected final LeftPaneController leftPaneController;
    protected final VBox root;
    private final GraphConfiguration graphConfiguration;

    public SchemaTableViewController(
            LeftPaneController leftPaneController,
            GraphConfiguration graphConfiguration
    ) {
        super(leftPaneController.getPrimaryController().getStage());
        this.graphConfiguration = graphConfiguration;

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

        TableView<SchemaUI> tableView = new TableView<>();
        tableView.editableProperty().bind(this.editToggleSwitch.selectedProperty());
        tableView.setFixedCellSize(30D);

        TableColumn<SchemaUI, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<SchemaUI, Boolean> delete = new TableColumn<>("delete");
        delete.setText("delete");
        delete.setCellValueFactory(p -> p.getValue().deleteProperty());
        delete.setCellFactory(CheckBoxTableCell.forTableColumn(delete));
        delete.setPrefWidth(60);

        tableView.getColumns().addAll(nameColumn, delete);

        tableView.setItems(graphConfiguration.getSchemaUis());

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setPadding(new Insets(0, 5, 0, 0));
        Button save = new Button("Save");
        ButtonBar.setButtonData(save, ButtonBar.ButtonData.OK_DONE);
        Button cancel = new Button("Cancel");
        ButtonBar.setButtonData(cancel, ButtonBar.ButtonData.CANCEL_CLOSE);
        buttonBar.getButtons().addAll(save, cancel);
        VBox vBox = new VBox(5, tableView, buttonBar);
        vBox.setPadding(new Insets(0, 0, 5, 0));
        VBox.setVgrow(vBox, Priority.ALWAYS);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(buttonBar, Priority.NEVER);

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
