package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.ISqlgTopologyUI;
import org.umlg.sqlg.structure.SqlgGraph;

import java.util.Collection;

public abstract class BaseNameFormController extends BaseController {

    public static final int TOP_LABEL_MIN_WIDTH = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseNameFormController.class);
    protected final ToggleSwitch editToggleSwitch;
    protected final LeftPaneController leftPaneController;
    protected final ISqlgTopologyUI sqlgTopologyUI;
    protected final VBox root;
    protected final TextField sqlgTreeDataFormNameTxt;

    public BaseNameFormController(LeftPaneController leftPaneController, ISqlgTopologyUI sqlgTopologyUI) {
        super(leftPaneController.getPrimaryController().getStage());
        this.leftPaneController = leftPaneController;
        this.sqlgTopologyUI = sqlgTopologyUI;
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

        GridPane nameGridPane = new GridPane();
        nameGridPane.getStyleClass().add("sqlgfx-name-box");
        nameGridPane.setHgap(100);
        nameGridPane.setVgap(5);
        nameGridPane.setPadding(new Insets(5, 5, 5, 5));
        int rowIndex = 0;
        Label label = new Label("name");
        GridPane.setConstraints(label, 0, rowIndex);
        //Do not bind the name property as deletion happens via the old name.
        this.sqlgTreeDataFormNameTxt = new TextField(this.sqlgTopologyUI.getName());
        this.sqlgTreeDataFormNameTxt.setMinWidth(100);
        this.sqlgTreeDataFormNameTxt.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(this.sqlgTreeDataFormNameTxt, 1, rowIndex);
        rowIndex++;

        Button rename = new Button("Rename");
        Button delete = new Button("Delete");
        Button cancel = new Button("Cancel");

        HBox nameButtonBox = new HBox(5);
        nameButtonBox.setAlignment(Pos.CENTER_RIGHT);
        nameButtonBox.getChildren().addAll(rename, delete, cancel);
        GridPane.setConstraints(nameButtonBox, 1, rowIndex);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(80);
        column2.setHgrow(Priority.ALWAYS);
        nameGridPane.getColumnConstraints().addAll(column1, column2); // each get 50% of width
        nameGridPane.getChildren().addAll(label, this.sqlgTreeDataFormNameTxt, nameButtonBox);
        sqlgTreeDataFormNameTxt.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));

        rename.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        delete.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        cancel.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));

        this.root.getChildren().add(editBox);
        this.root.getChildren().add(nameGridPane);

        this.root.getChildren().addAll(additionalChildren(sqlgTopologyUI));

        rename.setOnAction(ignore -> rename());
        delete.setOnAction(ignore -> {
            SqlgGraph sqlgGraph = getSqlgGraph();
            try {
                delete();
                sqlgGraph.tx().commit();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                showDialog(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to save PropertyColumn",
                        e,
                        result -> {
                        }
                );
            } finally {
                sqlgGraph.tx().rollback();
            }
        });
        cancel.setOnAction(ignore -> {
            SqlgGraph sqlgGraph = getSqlgGraph();
            try {
                cancelName();
                sqlgGraph.tx().commit();
            } finally {
                sqlgGraph.tx().rollback();
            }
        });
    }

    protected abstract SqlgGraph getSqlgGraph();

    protected abstract void rename();

    protected void delete() {

    }

    protected void cancelName() {

    }

    public Parent getView() {
        return this.root;
    }


    protected abstract Collection<Node> additionalChildren(ISqlgTopologyUI sqlgTopologyUI);

}
