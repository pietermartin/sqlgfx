package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;
import org.sqlg.ui.model.EdgeLabelUI;
import org.sqlg.ui.model.PartitionUI;
import org.sqlg.ui.model.PropertyColumnUI;
import org.sqlg.ui.model.VertexLabelUI;

public class PartitionTableViewController extends BaseController {

    protected final ToggleSwitch editToggleSwitch;
    protected final LeftPaneController leftPaneController;
    protected final VBox root;
    private final VertexLabelUI vertexLabelUI;
    private final EdgeLabelUI edgeLabelUI;
    private final PartitionUI partitionUI;

    public PartitionTableViewController(
            LeftPaneController leftPaneController,
            VertexLabelUI vertexLabelUI,
            EdgeLabelUI edgeLabelUI,
            PartitionUI partitionUI
    ) {
        super(leftPaneController.getPrimaryController().getStage());
        assert vertexLabelUI == null || edgeLabelUI == null || partitionUI == null: "One of vertexLabelUI or edgeLabelUI or partitionUI expected.";
        this.vertexLabelUI = vertexLabelUI;
        this.edgeLabelUI = edgeLabelUI;
        this.partitionUI = partitionUI;

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
        init(vertexLabelUI, edgeLabelUI, partitionUI);
    }

    private void init(VertexLabelUI vertexLabelUI, EdgeLabelUI edgeLabelUI, PartitionUI partitionUI) {
        TableView<PartitionUI> tableView = new TableView<>();
        tableView.editableProperty().bind(this.editToggleSwitch.selectedProperty());
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);

//        private String from;
//        private String to;
//        private String in;
//        private Integer modulus;
//        private Integer remainder;

        TableColumn<PartitionUI, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<PartitionUI, String> partitionTypeColumn = new TableColumn<>("partitionType");
        partitionTypeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        partitionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("partitionType"));
        TableColumn<PartitionUI, String> partitionExpressionColumn = new TableColumn<>("partitionExpression");
        partitionExpressionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        partitionExpressionColumn.setCellValueFactory(new PropertyValueFactory<>("partitionExpression"));

        TableColumn<PartitionUI, String> fromColumn = new TableColumn<>("from");
        fromColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        TableColumn<PartitionUI, String> toColumn = new TableColumn<>("to");
        toColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        TableColumn<PartitionUI, String> inColumn = new TableColumn<>("in");
        inColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        inColumn.setCellValueFactory(new PropertyValueFactory<>("in"));

        tableView.getColumns().addAll(nameColumn, partitionTypeColumn, partitionExpressionColumn, fromColumn, toColumn, inColumn);

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
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(vBox, Priority.ALWAYS);

        save.disableProperty().bind(Bindings.createBooleanBinding(() -> !this.editToggleSwitch.selectedProperty().get(), this.editToggleSwitch.selectedProperty()));
        save.setOnAction(ignore -> {

        });
        cancel.setOnAction(ignore -> {

        });

        if (vertexLabelUI != null) {
            tableView.setItems(vertexLabelUI.getPartitionUIs());
        } else if (edgeLabelUI != null) {
            tableView.setItems(edgeLabelUI.getPartitionUIs());
        } else {
            tableView.setItems(partitionUI.getSubPartitionUIs());
        }
        this.root.getChildren().add(vBox);
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
