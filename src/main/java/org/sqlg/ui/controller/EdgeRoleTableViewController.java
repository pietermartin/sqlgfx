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
import org.sqlg.ui.model.EdgeLabelUI;
import org.sqlg.ui.model.EdgeRoleUI;
import org.sqlg.ui.model.VertexLabelUI;
import org.umlg.sqlg.structure.SqlgGraph;

import java.util.Arrays;
import java.util.List;

public class EdgeRoleTableViewController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeRoleTableViewController.class);
    protected final ToggleSwitch editToggleSwitch;
    protected final LeftPaneController leftPaneController;
    protected final VBox root;
    private final VertexLabelUI vertexLabelUI;
    private final EdgeLabelUI edgeLabelUI;
    private final Direction direction;

    public EdgeRoleTableViewController(
            LeftPaneController leftPaneController,
            VertexLabelUI vertexLabelUI,
            EdgeLabelUI edgeLabelUI,
            Direction direction
    ) {
        super(leftPaneController.getPrimaryController().getStage());
        assert (vertexLabelUI != null && edgeLabelUI == null) || (vertexLabelUI == null && edgeLabelUI != null);
        this.vertexLabelUI = vertexLabelUI;
        this.edgeLabelUI = edgeLabelUI;
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
            if (vertexLabelUI != null) {
                tableView.setItems(vertexLabelUI.getOutEdgeRoleUIs());
            } else {
                tableView.setItems(edgeLabelUI.getOutEdgeRoleUIs());
            }
        } else {
            if (vertexLabelUI != null) {
                tableView.setItems(vertexLabelUI.getInEdgeRoleUIs());
            } else {
                tableView.setItems(edgeLabelUI.getInEdgeRoleUIs());
            }
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
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(vBox, Priority.ALWAYS);

        save.disableProperty().bind(Bindings.createBooleanBinding(() -> !this.editToggleSwitch.selectedProperty().get(), this.editToggleSwitch.selectedProperty()));
        save.setOnAction(x -> {
            doSave(tableView);
        });
        cancel.setOnAction(x -> {
            doCancel(tableView);
        });
        this.root.getChildren().add(vBox);
    }

    private void doCancel(TableView<EdgeRoleUI> tableView) {
        if (this.vertexLabelUI != null) {
            this.vertexLabelUI.refresh();
            if (direction == Direction.OUT) {
                tableView.setItems(vertexLabelUI.getOutEdgeRoleUIs());
            } else {
                tableView.setItems(vertexLabelUI.getInEdgeRoleUIs());
            }

        } else {
            this.edgeLabelUI.refresh();
            if (direction == Direction.OUT) {
                tableView.setItems(edgeLabelUI.getOutEdgeRoleUIs());
            } else {
                tableView.setItems(edgeLabelUI.getInEdgeRoleUIs());
            }
        }
    }

    private void doSave(TableView<EdgeRoleUI> tableView) {
        if (this.vertexLabelUI != null) {
            SqlgGraph sqlgGraph = this.vertexLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
            List<EdgeRoleUI> edgeRoleUISToDelete = this.direction == Direction.OUT ?
                    this.vertexLabelUI.getOutEdgeRoleUIs().stream().filter(EdgeRoleUI::isDelete).toList() :
                    this.vertexLabelUI.getInEdgeRoleUIs().stream().filter(EdgeRoleUI::isDelete).toList();
            String description = edgeRoleUISToDelete.stream()
                    .map(edgeRoleUI -> edgeRoleUI.getEdgeRole().getName())
                    .reduce((a,b) -> a + ", " + b).orElse("");
            try {
                for (EdgeRoleUI edgeRoleUI : edgeRoleUISToDelete) {
                    if (edgeRoleUI.isDelete()) {
                        if (this.direction == Direction.OUT) {
                            this.vertexLabelUI.getOutEdgeRoleUIs().remove(edgeRoleUI);
                        } else {
                            this.vertexLabelUI.getInEdgeRoleUIs().remove(edgeRoleUI);
                        }
                        edgeRoleUI.getEdgeRole().remove();
                    }
                }
                sqlgGraph.tx().commit();
                showDialog(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Deleted EdgeRoles '" + description + "'"
                );
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                sqlgGraph.tx().rollback();
                showDialog(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to delete EdgeRoles '" + description + "'",
                        e,
                        result -> doCancel(tableView)
                );
            } finally {
                sqlgGraph.tx().rollback();
            }
        } else {
            SqlgGraph sqlgGraph = this.edgeLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
            List<EdgeRoleUI> edgeRoleUISToDelete = this.direction == Direction.OUT ?
                    this.edgeLabelUI.getOutEdgeRoleUIs().stream().filter(EdgeRoleUI::isDelete).toList() :
                    this.edgeLabelUI.getInEdgeRoleUIs().stream().filter(EdgeRoleUI::isDelete).toList();
            String description = edgeRoleUISToDelete.stream()
                    .map(edgeRoleUI -> edgeRoleUI.getEdgeRole().getName())
                    .reduce((a,b) -> a + ", " + b).orElse("");
            try {
                for (EdgeRoleUI edgeRoleUI : edgeRoleUISToDelete) {
                    if (edgeRoleUI.isDelete()) {
                        if (this.direction == Direction.OUT) {
                            this.edgeLabelUI.getOutEdgeRoleUIs().remove(edgeRoleUI);
                        } else {
                            this.edgeLabelUI.getInEdgeRoleUIs().remove(edgeRoleUI);
                        }
                        edgeRoleUI.getEdgeRole().remove();
                    }
                }
                sqlgGraph.tx().commit();
                showDialog(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Deleted EdgeRoles '" + description + "'"
                );
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                sqlgGraph.tx().rollback();
                showDialog(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to delete EdgeRoles '" + description + "'",
                        e,
                        result -> doCancel(tableView)
                );
            } finally {
                sqlgGraph.tx().rollback();
            }
        }
    }

    private void cancel() {

    }

    public Parent getView() {
        return this.root;
    }
}
