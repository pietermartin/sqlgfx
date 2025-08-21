package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.LongStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.EdgeRoleUI;
import org.sqlg.ui.model.ISqlgTopologyUI;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.EdgeRole;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class EdgeRoleFormController extends AbstractLabelControllerName {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeRoleFormController.class);
    private final EdgeRoleUI edgeRoleUI;

    public EdgeRoleFormController(LeftPaneController leftPaneController, EdgeRoleUI edgeRoleUI) {
        super(leftPaneController, edgeRoleUI);
        this.edgeRoleUI = edgeRoleUI;
    }

    @Override
    protected SqlgGraph getSqlgGraph() {
        if (this.edgeRoleUI.getVertexLabelUI() != null) {
            return this.edgeRoleUI.getVertexLabelUI().getSchemaUI().getGraphConfiguration().getSqlgGraph();
        } else {
            return this.edgeRoleUI.getEdgeLabelUI().getSchemaUI().getGraphConfiguration().getSqlgGraph();
        }
    }

    @Override
    protected void delete() {
        try {
            EdgeRole edgeRole = this.edgeRoleUI.getEdgeRole();
            edgeRole.remove();
            if (true) {
                throw new RuntimeException("asdasd");
            }
            showDialog(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Deleted EdgeRole '" + this.sqlgTreeDataFormNameTxt.getText() + "'"
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            getSqlgGraph().tx().rollback();
            showDialog(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Failed to delete EdgeRole '" + this.sqlgTreeDataFormNameTxt.getText() + "'",
                    e,
                    ignore -> {
                        System.out.println("asd");
                    }
            );
        }
    }

    @Override
    protected void rename() {
        if (!this.edgeRoleUI.getEdgeRole().getName().equals(this.sqlgTreeDataFormNameTxt.getText())) {
            SqlgGraph sqlgGraph = getSqlgGraph();
            try {
                EdgeRole edgeRole = this.edgeRoleUI.getEdgeRole();
                edgeRole.rename(this.sqlgTreeDataFormNameTxt.getText());
                sqlgGraph.tx().commit();
                Platform.runLater(() -> {
//                    SchemaUI schemaUI = this.edgeRoleUI.getSchemaUI();
//                    GraphConfiguration graphConfiguration = this.edgeRoleUI.getSchemaUI().getGraphConfiguration();
//                    GraphGroup graphGroup = graphConfiguration.getGraphGroup();
//                    this.leftPaneController.selectEdgeLabel(
//                                    graphGroup,
//                                    graphConfiguration,
//                                    schemaUI.getSchema(),
//                                    this.sqlgTreeDataFormNameTxt.getText()
//                            );
                });
                showDialog(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Renamed EdgeRole to '" + this.sqlgTreeDataFormNameTxt.getText() + "'"
                );
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                showDialog(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to renamed EdgeRole to '" + this.sqlgTreeDataFormNameTxt.getText() + "'",
                        e,
                        result -> this.sqlgTreeDataFormNameTxt.setText(this.edgeRoleUI.getEdgeRole().getName())
                );
            } finally {
                sqlgGraph.tx().rollback();
            }
        }

    }

    @Override
    protected Collection<Node> additionalChildren(ISqlgTopologyUI sqlgTopologyUI) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };

        EdgeRoleUI edgeRoleUI = (EdgeRoleUI) sqlgTopologyUI;

        GridPane gridPane = new GridPane();
        gridPane.setHgap(100);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(0, 5, 0, 5));
        int rowIndex = 0;

        Label lowerLabel = new Label("lower");
        GridPane.setConstraints(lowerLabel, 0, rowIndex);
        TextField lowerTextField = new TextField();
        lowerTextField.setTextFormatter(new TextFormatter<>(filter));
        lowerTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        StringConverter<Long> converter = new LongStringConverter();
        Bindings.bindBidirectional(lowerTextField.textProperty(), edgeRoleUI.lowerProperty(), (StringConverter) converter);
        GridPane.setConstraints(lowerTextField, 1, rowIndex);
        rowIndex++;

        Label upperLabel = new Label("upper");
        GridPane.setConstraints(upperLabel, 0, rowIndex);
        TextField upperTextField = new TextField();
//        upperTextField.setTextFormatter(new TextFormatter<>(filter));
        upperTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        Bindings.bindBidirectional(upperTextField.textProperty(), edgeRoleUI.upperProperty(), (StringConverter) converter);
        GridPane.setConstraints(upperTextField, 1, rowIndex);

        rowIndex++;
        Label uniqueLabel = new Label("unique");
        GridPane.setConstraints(uniqueLabel, 0, rowIndex);
        CheckBox uniqueCheckBox = new CheckBox();
        uniqueCheckBox.setSelected(edgeRoleUI.isUnique());
        uniqueCheckBox.setDisable(true);
        GridPane.setConstraints(uniqueCheckBox, 1, rowIndex);

        rowIndex++;
        Label orederedLabel = new Label("ordered");
        GridPane.setConstraints(orederedLabel, 0, rowIndex);
        CheckBox orderedCheckBox = new CheckBox();
        orderedCheckBox.setSelected(edgeRoleUI.isUnique());
        orderedCheckBox.setDisable(true);
        GridPane.setConstraints(orderedCheckBox, 1, rowIndex);

        rowIndex++;
        Label directionLabel = new Label("direction");
        GridPane.setConstraints(directionLabel, 0, rowIndex);
        TextField directionTextField = new TextField();
        directionTextField.setText(edgeRoleUI.getDirection().name());
        directionTextField.setDisable(true);
        GridPane.setConstraints(directionTextField, 1, rowIndex);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(80);
        column2.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(column1, column2); // each get 50% of width
        gridPane.getChildren().addAll(
                lowerLabel, lowerTextField,
                upperLabel, upperTextField,
                uniqueLabel, uniqueCheckBox,
                orederedLabel, orderedCheckBox,
                directionLabel, directionTextField
        );


        VBox.setVgrow(gridPane, Priority.ALWAYS);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setPadding(new Insets(0, 5, 0, 0));
        Button save = new Button("Save");
        ButtonBar.setButtonData(save, ButtonBar.ButtonData.OK_DONE);
        Button cancel = new Button("Cancel");
        ButtonBar.setButtonData(cancel, ButtonBar.ButtonData.CANCEL_CLOSE);
        buttonBar.getButtons().addAll(save, cancel);

        VBox vBox = new VBox(5, gridPane, buttonBar);
        vBox.setPadding(new Insets(0, 0, 5, 0));
        VBox.setVgrow(buttonBar, Priority.NEVER);
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        VBox.setVgrow(vBox, Priority.ALWAYS);

        save.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        save.setOnAction(ignore -> save());
        cancel.setOnAction(ignore -> cancel());
        return List.of(vBox);
    }

    void cancel() {
    }

    void save() {
    }

}
