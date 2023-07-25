package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;
import org.sqlg.ui.model.ISqlgTopologyUI;
import org.umlg.sqlg.structure.SqlgGraph;

import java.util.Collection;

public abstract class BaseNameFormController extends BaseController {

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
        this.root.setPadding(new Insets(10, 10, 10, 10));
        this.root.setMaxHeight(Double.MAX_VALUE);

        this.editToggleSwitch = new ToggleSwitch("Edit");
        this.editToggleSwitch.setLayoutX(70);
        this.editToggleSwitch.setLayoutY(168);
        HBox editBox = new HBox();
        editBox.setPadding(new Insets(30, 30, 0, 30));
        editBox.setAlignment(Pos.CENTER_RIGHT);
        editBox.getChildren().addAll(editToggleSwitch);

        HBox nameHBox = new HBox(5);
        Label label = new Label("name");
        nameHBox.setAlignment(Pos.CENTER);

        this.sqlgTreeDataFormNameTxt = new TextField(this.sqlgTopologyUI.getName());
        this.sqlgTreeDataFormNameTxt.setMaxWidth(Double.MAX_VALUE);
        Button rename = new Button("Rename");
        Button delete = new Button("Delete");
        HBox.setHgrow(this.sqlgTreeDataFormNameTxt, Priority.ALWAYS);

        nameHBox.getChildren().addAll(label, this.sqlgTreeDataFormNameTxt, rename, delete);

        sqlgTreeDataFormNameTxt.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));

        VBox.setVgrow(nameHBox, Priority.NEVER);

        rename.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        delete.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));

        this.root.getChildren().add(editBox);
        this.root.getChildren().add(nameHBox);
        this.root.getChildren().addAll(additionalChildren(sqlgTopologyUI));

        rename.setOnAction(event -> {
            rename();
        });
        delete.setOnAction(event -> {
            SqlgGraph sqlgGraph = getSqlgGraph();
            try {
                delete();
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

    public Parent getView() {
        return this.root;
    }


    protected abstract Collection<Node> additionalChildren(ISqlgTopologyUI sqlgTopologyUI);

}
