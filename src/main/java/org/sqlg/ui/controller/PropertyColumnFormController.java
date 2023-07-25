package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.*;
import org.umlg.sqlg.structure.PropertyType;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.AbstractLabel;
import org.umlg.sqlg.structure.topology.PropertyColumn;

import java.util.Collection;
import java.util.List;

public class PropertyColumnFormController extends BaseNameFormController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyColumnFormController.class);
    private final PropertyColumnUI propertyColumnUI;
    private ComboBox<PropertyType> propertyTypeCombobox;
    private TextField lowerTextField;
    private TextField upperTextField;
    private CheckBox uniqueCheckBox;
    private CheckBox orderedCheckBox;
    private TextField defaultLiteralTextField;
    private TextField checkConstraintTextField;

    public PropertyColumnFormController(LeftPaneController leftPaneController, PropertyColumnUI propertyColumnUI) {
        super(leftPaneController, propertyColumnUI);
        this.propertyColumnUI = propertyColumnUI;
    }

    @Override
    protected SqlgGraph getSqlgGraph() {
        if (this.propertyColumnUI.getVertexLabelUI() != null) {
            return this.propertyColumnUI.getVertexLabelUI().getSchemaUI().getGraphConfiguration().getSqlgGraph();
        } else {
            return this.propertyColumnUI.getEdgeLabelUI().getSchemaUI().getGraphConfiguration().getSqlgGraph();
        }
    }

    @Override
    protected void delete() {
        VertexLabelUI vertexLabelUI = this.propertyColumnUI.getVertexLabelUI();
        if (vertexLabelUI == null) {
            EdgeLabelUI edgeLabelUI = this.propertyColumnUI.getEdgeLabelUI();
            SchemaUI schemaUI = edgeLabelUI.getSchemaUI();
            GraphConfiguration graphConfiguration = schemaUI.getGraphConfiguration();
            GraphGroup graphGroup = graphConfiguration.getGraphGroup();
            edgeLabelUI.getPropertyColumnUIs().remove(this.propertyColumnUI);
            this.propertyColumnUI.getPropertyColumn().remove();
            this.leftPaneController.deletePropertyColumn(
                    graphGroup,
                    graphConfiguration,
                    schemaUI.getSchema(),
                    null,
                    edgeLabelUI.getEdgeLabel(),
                    this.propertyColumnUI.getPropertyColumn()
            );
        } else {
            SchemaUI schemaUI = vertexLabelUI.getSchemaUI();
            GraphConfiguration graphConfiguration = schemaUI.getGraphConfiguration();
            GraphGroup graphGroup = graphConfiguration.getGraphGroup();
            vertexLabelUI.getPropertyColumnUIs().remove(this.propertyColumnUI);
            this.propertyColumnUI.getPropertyColumn().remove();
            this.leftPaneController.deletePropertyColumn(
                    graphGroup,
                    graphConfiguration,
                    schemaUI.getSchema(),
                    vertexLabelUI.getVertexLabel(),
                    null,
                    this.propertyColumnUI.getPropertyColumn()
            );
        }
    }

    @Override
    protected void rename() {
        SqlgGraph sqlgGraph = getSqlgGraph();
        try {
            AbstractLabel abstractLabel = this.propertyColumnUI.getPropertyColumn().getParentLabel();
            this.propertyColumnUI.getPropertyColumn().rename(this.sqlgTreeDataFormNameTxt.getText());
            sqlgGraph.tx().commit();
            PropertyColumn renamePropertyColumn = abstractLabel.getProperty(this.sqlgTreeDataFormNameTxt.getText()).orElseThrow();
            this.propertyColumnUI.setPropertyColumn(renamePropertyColumn);
            showDialog(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Renamed PropertyColumn to '" + this.sqlgTreeDataFormNameTxt.getText() + "'"
            );
        } catch (Exception e) {
            showDialog(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Failed to renamed PropertyColumn to '" + this.sqlgTreeDataFormNameTxt.getText() + "'",
                    e,
                    result -> this.sqlgTreeDataFormNameTxt.setText(this.propertyColumnUI.getPropertyColumn().getName())
            );
        } finally {
            sqlgGraph.tx().rollback();
        }
    }


    @Override
    protected Collection<Node> additionalChildren(ISqlgTopologyUI sqlgTopologyUI) {
        PropertyColumnUI propertyColumnUI = (PropertyColumnUI) sqlgTopologyUI;

        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);

        Label propertyTypeLabel = new Label("PropertyType");
        GridPane.setConstraints(propertyTypeLabel, 0, 0);

        this.propertyTypeCombobox = new ComboBox<>();
        this.propertyTypeCombobox.getItems().addAll(PropertyType.values());
        this.propertyTypeCombobox.setPrefWidth(Double.MAX_VALUE);
        this.propertyTypeCombobox.setValue(PropertyType.STRING);
        this.propertyTypeCombobox.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        GridPane.setConstraints(this.propertyTypeCombobox, 1, 0);

        Label lowerLabel = new Label("lower");
        GridPane.setConstraints(lowerLabel, 0, 1);
        this.lowerTextField = new TextField(Long.toString(propertyColumnUI.getLower()));
        this.lowerTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                lowerTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        this.lowerTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        GridPane.setConstraints(this.lowerTextField, 1, 1);

        Label upperLabel = new Label("upper");
        GridPane.setConstraints(upperLabel, 0, 2);
        this.upperTextField = new TextField(Long.toString(propertyColumnUI.getUpper()));
        this.upperTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                upperTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        this.upperTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        GridPane.setConstraints(this.upperTextField, 1, 2);

        Label uniqueLabel = new Label("unique");
        GridPane.setConstraints(uniqueLabel, 0, 3);
        this.uniqueCheckBox = new CheckBox();
        this.uniqueCheckBox.setSelected(propertyColumnUI.isUnique());
        this.uniqueCheckBox.setDisable(true);
        GridPane.setConstraints(this.uniqueCheckBox, 1, 3);

        Label orderedLabel = new Label("ordered");
        GridPane.setConstraints(orderedLabel, 0, 4);
        this.orderedCheckBox = new CheckBox();
        this.orderedCheckBox.setSelected(propertyColumnUI.isOrdered());
        this.orderedCheckBox.setDisable(true);
        GridPane.setConstraints(this.orderedCheckBox, 1, 4);

        Label defaultLiteralLabel = new Label("defaultLiteral");
        GridPane.setConstraints(defaultLiteralLabel, 0, 5);
        this.defaultLiteralTextField = new TextField(propertyColumnUI.getDefaultLiteral());
        this.defaultLiteralTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        GridPane.setConstraints(this.defaultLiteralTextField, 1, 5);

        Label checkConstraintLabel = new Label("checkConstraint");
        GridPane.setConstraints(checkConstraintLabel, 0, 6);
        this.checkConstraintTextField = new TextField(propertyColumnUI.getCheckConstraint());
        this.checkConstraintTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        GridPane.setConstraints(this.checkConstraintTextField, 1, 6);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(80);
        gridPane.getColumnConstraints().addAll(column1, column2); // each get 50% of width
        gridPane.getChildren().addAll(
                propertyTypeLabel, this.propertyTypeCombobox,
                lowerLabel, this.lowerTextField,
                upperLabel, this.upperTextField,
                uniqueLabel, this.uniqueCheckBox,
                orderedLabel, this.orderedCheckBox,
                defaultLiteralLabel, this.defaultLiteralTextField,
                checkConstraintLabel, this.checkConstraintTextField
        );

        VBox.setVgrow(gridPane, Priority.ALWAYS);
        return List.of(gridPane);
    }

}
