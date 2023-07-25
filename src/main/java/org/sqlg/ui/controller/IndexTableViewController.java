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
import org.sqlg.ui.TopologyTreeItem;
import org.sqlg.ui.model.EdgeLabelUI;
import org.sqlg.ui.model.IndexUI;
import org.sqlg.ui.model.VertexLabelUI;

public class IndexTableViewController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexTableViewController.class);
    protected final ToggleSwitch editToggleSwitch;
    protected final LeftPaneController leftPaneController;
    protected final VBox root;
    private final VertexLabelUI vertexLabelUI;
    private final EdgeLabelUI edgeLabelUI;

    public IndexTableViewController(
            LeftPaneController leftPaneController,
            VertexLabelUI vertexLabelUI,
            EdgeLabelUI edgeLabelUI
    ) {
        super(leftPaneController.getPrimaryController().getStage());
        assert (vertexLabelUI == null && edgeLabelUI != null) || (vertexLabelUI != null && edgeLabelUI == null) : "One of vertexLabelUI or edgeLabelUI must be null.";
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

        TableView<IndexUI> tableView = new TableView<>();
        tableView.editableProperty().bind(this.editToggleSwitch.selectedProperty());
        tableView.setFixedCellSize(30D);

        TableColumn<IndexUI, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<IndexUI, Boolean> delete = new TableColumn<>("delete");
        delete.setText("delete");
        delete.setCellValueFactory(p -> p.getValue().deleteProperty());
        delete.setCellFactory(CheckBoxTableCell.forTableColumn(delete));
        delete.setPrefWidth(60);

        tableView.getColumns().addAll(nameColumn, delete);

        if (vertexLabelUI != null) {
            tableView.setItems(vertexLabelUI.getIndexUIs());
        } else {
            tableView.setItems(edgeLabelUI.getIndexUIs());
        }

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setPadding(new Insets(0, 5, 0, 0));
        Button save = new Button("Save");
        ButtonBar.setButtonData(save, ButtonBar.ButtonData.OK_DONE);
        Button cancel = new Button("Cancel");
        ButtonBar.setButtonData(cancel, ButtonBar.ButtonData.CANCEL_CLOSE);
        buttonBar.getButtons().addAll(save, cancel);
        VBox vBox = new VBox(5, tableView, buttonBar);
        vBox.setPadding(new Insets(0, 0, 5, 0));
        VBox.setVgrow(buttonBar, Priority.NEVER);

        save.disableProperty().bind(Bindings.createBooleanBinding(() -> !this.editToggleSwitch.selectedProperty().get(), this.editToggleSwitch.selectedProperty()));
        save.setOnAction(x -> {

        });
        cancel.setOnAction(x -> {

        });
        TitledPane titledPane = new TitledPane(TopologyTreeItem.INDEXES, vBox);
        this.root.getChildren().add(titledPane);
    }

    private void save() {

    }

    private void cancel() {

    }

    public Parent getView() {
        return this.root;
    }
}
