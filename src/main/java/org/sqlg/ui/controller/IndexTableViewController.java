package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.EdgeLabelUI;
import org.sqlg.ui.model.IndexUI;
import org.sqlg.ui.model.PropertyColumnUI;
import org.sqlg.ui.model.VertexLabelUI;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.IndexType;
import org.umlg.sqlg.structure.topology.PropertyColumn;

import java.util.List;

public class IndexTableViewController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexTableViewController.class);
    protected final ToggleSwitch editToggleSwitch;
    protected final LeftPaneController leftPaneController;
    protected final VBox root;
    private TableView<IndexUI> indexTableView;
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

        this.indexTableView = new TableView<>();
        indexTableView.editableProperty().bind(this.editToggleSwitch.selectedProperty());
        indexTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);

        TableColumn<IndexUI, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setEditable(false);

        TableColumn<IndexUI, String> indexTypeColumn = new TableColumn<>("indexType");
        List<String> indexTypes = List.of(IndexType.UNIQUE.getName(), IndexType.NON_UNIQUE.getName(), IndexType.GIN.getName());
        indexTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(indexTypes.toArray(new String[]{})));
        indexTypeColumn.setCellValueFactory(p -> p.getValue().indexTypeProperty());
        indexTypeColumn.setEditable(false);

        TableColumn<IndexUI, Boolean> delete = new TableColumn<>("delete");
        delete.setText("delete");
        delete.setCellValueFactory(p -> p.getValue().deleteProperty());
        delete.setCellFactory(CheckBoxTableCell.forTableColumn(delete));
        delete.setPrefWidth(60);

        indexTableView.getColumns().addAll(nameColumn, indexTypeColumn, delete);

        if (vertexLabelUI != null) {
            indexTableView.setItems(vertexLabelUI.getIndexUIs());
        } else {
            indexTableView.setItems(edgeLabelUI.getIndexUIs());
        }

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setPadding(new Insets(0, 5, 0, 0));
        Button save = new Button("Save");
        ButtonBar.setButtonData(save, ButtonBar.ButtonData.OK_DONE);
        Button cancel = new Button("Cancel");
        ButtonBar.setButtonData(cancel, ButtonBar.ButtonData.CANCEL_CLOSE);
        buttonBar.getButtons().addAll(save, cancel);

        save.disableProperty().bind(Bindings.createBooleanBinding(() -> !this.editToggleSwitch.selectedProperty().get(), this.editToggleSwitch.selectedProperty()));
        save.setOnAction(x -> {
            save();
        });
        cancel.setOnAction(x -> {
            cancel();
        });

        TableView<PropertyColumnUI> indexPropertyTableView = new TableView<>();
        indexPropertyTableView.setEditable(false);
        indexPropertyTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);

        TableColumn<PropertyColumnUI, String> propertyNameColumn = new TableColumn<>("name");
        propertyNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        propertyNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<PropertyColumnUI, String> propertyTypeColumn = new TableColumn<>("propertyType");
        propertyTypeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        propertyTypeColumn.setCellValueFactory(new PropertyValueFactory<>("propertyType"));

        indexPropertyTableView.getColumns().addAll(propertyNameColumn, propertyTypeColumn);

        ObservableList<PropertyColumnUI> propertyColumnUIS = FXCollections.observableArrayList();
        indexPropertyTableView.setItems(propertyColumnUIS);

        indexTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, indexUI) -> {
            propertyColumnUIS.clear();
            for (PropertyColumn propertyColumn : indexUI.getIndex().getProperties()) {
                propertyColumnUIS.add(new PropertyColumnUI(this.vertexLabelUI, this.edgeLabelUI, propertyColumn));
            }
        });

        VBox vBox = new VBox(5, indexTableView, buttonBar, indexPropertyTableView);
        vBox.setPadding(new Insets(0, 0, 0, 0));
        VBox.setVgrow(vBox, Priority.ALWAYS);
        VBox.setVgrow(indexTableView, Priority.ALWAYS);
        VBox.setVgrow(buttonBar, Priority.NEVER);

        this.root.getChildren().add(vBox);
    }

    private void save() {
        if (this.vertexLabelUI != null) {
            SqlgGraph sqlgGraph = this.vertexLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
            try {
                for (IndexUI indexUI : this.vertexLabelUI.getIndexUIs()) {
                    if (indexUI.isDelete()) {
                        indexUI.getIndex().remove();
                    }
                }
                sqlgGraph.tx().commit();
                showDialog(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Deleted index"
                );
            } finally {
                sqlgGraph.tx().rollback();
            }
        } else {
            SqlgGraph sqlgGraph = this.edgeLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
            try {
                for (IndexUI indexUI : this.edgeLabelUI.getIndexUIs()) {
                    if (indexUI.isDelete()) {
                        indexUI.getIndex().remove();
                    }
                }
                sqlgGraph.tx().commit();
                showDialog(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Deleted Index"
                );
            } finally {
                sqlgGraph.tx().rollback();
            }
        }
    }

    private void cancel() {
        if (this.vertexLabelUI != null) {
            for (IndexUI indexUI : this.vertexLabelUI.getIndexUIs()) {
                indexUI.reset();
            }
        }
        if (this.edgeLabelUI != null) {
            for (IndexUI indexUI : this.edgeLabelUI.getIndexUIs()) {
                indexUI.reset();
            }
        }
//        if (vertexLabelUI != null) {
//            indexTableView.setItems(vertexLabelUI.getIndexUIs());
//        } else {
//            indexTableView.setItems(edgeLabelUI.getIndexUIs());
//        }
    }

    public Parent getView() {
        return this.root;
    }
}
