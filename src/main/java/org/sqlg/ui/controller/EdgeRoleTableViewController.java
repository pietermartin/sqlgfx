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
import javafx.util.StringConverter;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.tableview2.cell.ComboBox2TableCell;
import org.controlsfx.control.tableview2.cell.TextField2TableCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.TopologyTreeItem;
import org.sqlg.ui.model.EdgeRoleUI;
import org.sqlg.ui.model.VertexLabelUI;

import java.util.Arrays;

public class EdgeRoleTableViewController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeRoleTableViewController.class);
    protected final ToggleSwitch editToggleSwitch;
    protected final LeftPaneController leftPaneController;
    protected final VBox root;
    private final VertexLabelUI vertexLabelUI;
    private final Direction direction;

    public EdgeRoleTableViewController(
            LeftPaneController leftPaneController,
            VertexLabelUI vertexLabelUI,
            Direction direction
    ) {
        super(leftPaneController.getPrimaryController().getStage());
        this.vertexLabelUI = vertexLabelUI;
        this.direction = direction;

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

        TableView<EdgeRoleUI> tableView = new TableView<>();
        tableView.editableProperty().bind(this.editToggleSwitch.selectedProperty());
        tableView.setFixedCellSize(30D);

        TableColumn<EdgeRoleUI, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<EdgeRoleUI, Direction> directionColumn = new TableColumn<>("direction");
        directionColumn.setCellFactory(ComboBox2TableCell.forTableColumn(Arrays.stream(Direction.values()).toList().toArray(new Direction[]{})));
        directionColumn.setCellValueFactory(p -> p.getValue().directionProperty());
        directionColumn.setEditable(false);

        //Multiplicity start
        TableColumn<EdgeRoleUI, Long> lowerColumn = new TableColumn<>("lower");
        lowerColumn.setCellValueFactory(p -> p.getValue().lowerProperty().asObject());
        lowerColumn.setCellFactory(TextField2TableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(Long object) {
                return String.valueOf(object);
            }

            @Override
            public Long fromString(String string) {
                return Long.parseLong(string);
            }
        }));
        TableColumn<EdgeRoleUI, Long> upperColumn = new TableColumn<>("upper");
        upperColumn.setCellValueFactory(p -> p.getValue().upperProperty().asObject());
        upperColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(Long object) {
                return String.valueOf(object);
            }

            @Override
            public Long fromString(String string) {
                return Long.parseLong(string);
            }
        }));

        TableColumn<EdgeRoleUI, Boolean> uniqueColumn = new TableColumn<>("unique");
        uniqueColumn.setCellValueFactory(p -> p.getValue().uniqueProperty().asObject());
        TableColumn<EdgeRoleUI, Boolean> orderedColumn = new TableColumn<>("ordered");
        orderedColumn.setCellValueFactory(p -> p.getValue().orderedProperty().asObject());
        //Multiplicity end
        TableColumn<EdgeRoleUI, ?> multiplicityColumn = new TableColumn<>("Multiplicity");
        multiplicityColumn.getColumns().addAll(lowerColumn, upperColumn, uniqueColumn, orderedColumn);


        TableColumn<EdgeRoleUI, Boolean> delete = new TableColumn<>("delete");
        delete.setText("delete");
        delete.setCellValueFactory(p -> p.getValue().deleteProperty());
        delete.setCellFactory(CheckBoxTableCell.forTableColumn(delete));
        delete.setPrefWidth(60);


        tableView.getColumns().addAll(nameColumn, multiplicityColumn, directionColumn, delete);

        if (direction == Direction.OUT) {
            tableView.setItems(vertexLabelUI.getOutEdgeRoleUIs());
        } else {
            tableView.setItems(vertexLabelUI.getInEdgeRoleUIs());
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
        TitledPane titledPane = new TitledPane(direction == Direction.OUT ? TopologyTreeItem.OUT_EDGE_ROLES : TopologyTreeItem.IN_EDGE_ROLES, vBox);
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
