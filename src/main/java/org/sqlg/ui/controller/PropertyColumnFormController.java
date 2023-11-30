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
import org.sqlg.ui.model.*;
import org.umlg.sqlg.structure.Multiplicity;
import org.umlg.sqlg.structure.PropertyDefinition;
import org.umlg.sqlg.structure.PropertyType;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.AbstractLabel;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class PropertyColumnFormController extends BaseNameFormController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyColumnFormController.class);
    private final PropertyColumnUI propertyColumnUI;

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
        this.propertyColumnUI.getPropertyColumn().remove();
    }

    @Override
    protected void rename() {
        SqlgGraph sqlgGraph = getSqlgGraph();
        try {
            this.propertyColumnUI.getPropertyColumn().rename(this.sqlgTreeDataFormNameTxt.getText());
            sqlgGraph.tx().commit();
            Platform.runLater(() -> {

                if (this.propertyColumnUI.getVertexLabelUI() != null) {
                    VertexLabelUI vertexLabelUI = this.propertyColumnUI.getVertexLabelUI();
                    SchemaUI schemaUI = vertexLabelUI.getSchemaUI();
                    GraphConfiguration graphConfiguration = schemaUI.getGraphConfiguration();
                    GraphGroup graphGroup = graphConfiguration.getGraphGroup();
                    this.leftPaneController.selectPropertyColumn(
                                    graphGroup,
                                    graphConfiguration,
                                    schemaUI.getSchema(),
                                    vertexLabelUI.getVertexLabel(),
                                    this.sqlgTreeDataFormNameTxt.getText()
                            );
                } else {
                    EdgeLabelUI edgeLabelUI = this.propertyColumnUI.getEdgeLabelUI();
                    SchemaUI schemaUI = edgeLabelUI.getSchemaUI();
                    GraphConfiguration graphConfiguration = schemaUI.getGraphConfiguration();
                    GraphGroup graphGroup = graphConfiguration.getGraphGroup();
                    this.leftPaneController.selectPropertyColumn(
                                    graphGroup,
                                    graphConfiguration,
                                    schemaUI.getSchema(),
                                    edgeLabelUI.getEdgeLabel(),
                                    this.sqlgTreeDataFormNameTxt.getText()
                            );
                }
            });
            showDialog(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Renamed PropertyColumn to '" + this.sqlgTreeDataFormNameTxt.getText() + "'"
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
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
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };

        PropertyColumnUI propertyColumnUI = (PropertyColumnUI) sqlgTopologyUI;

        GridPane gridPane = new GridPane();
        gridPane.setHgap(100);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(0, 5, 0, 5));
        int rowIndex = 0;

        Label identifierLabel = new Label("identifier");
        GridPane.setConstraints(identifierLabel, 0, rowIndex);
        CheckBox identifierCheckBox = new CheckBox();
        identifierCheckBox.setSelected(propertyColumnUI.isIdentifier());
        identifierCheckBox.setDisable(true);
        GridPane.setConstraints(identifierCheckBox, 1, rowIndex);
        rowIndex++;

        Label propertyTypeLabel = new Label("PropertyType");
        GridPane.setConstraints(propertyTypeLabel, 0, rowIndex);

        ComboBox<PropertyType> propertyTypeCombobox = new ComboBox<>();
        propertyTypeCombobox.getItems().addAll(PropertyType.values());
        propertyTypeCombobox.setPrefWidth(Double.MAX_VALUE);
        propertyTypeCombobox.setValue(PropertyType.STRING);
        propertyTypeCombobox.setDisable(true);
        GridPane.setConstraints(propertyTypeCombobox, 1, rowIndex);
        rowIndex++;

        Label lowerLabel = new Label("lower");
        GridPane.setConstraints(lowerLabel, 0, rowIndex);
        TextField lowerTextField = new TextField(Long.toString(propertyColumnUI.getLower()));
        lowerTextField.setTextFormatter(new TextFormatter<>(filter));
        lowerTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        StringConverter<? extends Number> converter = new LongStringConverter();
        Bindings.bindBidirectional(lowerTextField.textProperty(), propertyColumnUI.lowerProperty(), (StringConverter) converter);
        GridPane.setConstraints(lowerTextField, 1, rowIndex);
        rowIndex++;

        Label upperLabel = new Label("upper");
        GridPane.setConstraints(upperLabel, 0, rowIndex);
        TextField upperTextField = new TextField(Long.toString(propertyColumnUI.getUpper()));
        upperTextField.setTextFormatter(new TextFormatter<>(filter));
        Bindings.bindBidirectional(upperTextField.textProperty(), propertyColumnUI.upperProperty(), (StringConverter) converter);
        upperTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        GridPane.setConstraints(upperTextField, 1, rowIndex);
        rowIndex++;

        Label uniqueLabel = new Label("unique");
        GridPane.setConstraints(uniqueLabel, 0, rowIndex);
        CheckBox uniqueCheckBox = new CheckBox();
        uniqueCheckBox.setSelected(propertyColumnUI.isUnique());
        uniqueCheckBox.setDisable(true);
        GridPane.setConstraints(uniqueCheckBox, 1, rowIndex);
        rowIndex++;

        Label orderedLabel = new Label("ordered");
        GridPane.setConstraints(orderedLabel, 0, rowIndex);
        CheckBox orderedCheckBox = new CheckBox();
        orderedCheckBox.setSelected(propertyColumnUI.isOrdered());
        orderedCheckBox.setDisable(true);
        GridPane.setConstraints(orderedCheckBox, 1, rowIndex);
        rowIndex++;

        Label defaultLiteralLabel = new Label("defaultLiteral");
        GridPane.setConstraints(defaultLiteralLabel, 0, rowIndex);
        TextField defaultLiteralTextField = new TextField(propertyColumnUI.getDefaultLiteral());
        defaultLiteralTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        Bindings.bindBidirectional(defaultLiteralTextField.textProperty(), propertyColumnUI.defaultLiteralProperty());
        GridPane.setConstraints(defaultLiteralTextField, 1, rowIndex);
        rowIndex++;

        Label checkConstraintLabel = new Label("checkConstraint");
        GridPane.setConstraints(checkConstraintLabel, 0, rowIndex);
        TextField checkConstraintTextField = new TextField(propertyColumnUI.getCheckConstraint());
        checkConstraintTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        Bindings.bindBidirectional(checkConstraintTextField.textProperty(), propertyColumnUI.checkConstraintProperty());
        GridPane.setConstraints(checkConstraintTextField, 1, rowIndex);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(80);
        column2.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(column1, column2); // each get 50% of width
        gridPane.getChildren().addAll(
                identifierLabel, identifierCheckBox,
                propertyTypeLabel, propertyTypeCombobox,
                lowerLabel, lowerTextField,
                upperLabel, upperTextField,
                uniqueLabel, uniqueCheckBox,
                orderedLabel, orderedCheckBox,
                defaultLiteralLabel, defaultLiteralTextField,
                checkConstraintLabel, checkConstraintTextField
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
        this.propertyColumnUI.reset();
        this.getSqlgGraph().tx().rollback();
    }

    void save() {
        try {
            if (this.propertyColumnUI.getLower() != this.propertyColumnUI.getPropertyColumn().getPropertyDefinition().multiplicity().lower() ||
                            this.propertyColumnUI.getUpper() != this.propertyColumnUI.getPropertyColumn().getPropertyDefinition().multiplicity().upper() ||
                            !this.propertyColumnUI.getDefaultLiteral().equals(this.propertyColumnUI.getPropertyColumn().getPropertyDefinition().defaultLiteral()) ||
                            !this.propertyColumnUI.getCheckConstraint().equals(this.propertyColumnUI.getPropertyColumn().getPropertyDefinition().checkConstraint())
            ) {
                PropertyDefinition updatedPropertyDefinition = PropertyDefinition.of(
                        PropertyType.valueOf(this.propertyColumnUI.getPropertyType()),
                        Multiplicity.of(
                                this.propertyColumnUI.getLower(),
                                this.propertyColumnUI.getUpper(),
                                this.propertyColumnUI.isUnique()
                        ),
                        this.propertyColumnUI.getDefaultLiteral(),
                        this.propertyColumnUI.getCheckConstraint()
                );
                AbstractLabel abstractLabel = this.propertyColumnUI.getPropertyColumn().getParentLabel();
                String propertyName = this.propertyColumnUI.getPropertyColumn().getName();
                this.propertyColumnUI.getPropertyColumn().updatePropertyDefinition(updatedPropertyDefinition);
                this.propertyColumnUI.setPropertyColumn(abstractLabel.getProperty(propertyName).orElseThrow());
            }
            getSqlgGraph().tx().commit();
            showDialog(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Saved PropertyColumns"
            );
        } catch (Exception e) {
            getSqlgGraph().tx().rollback();
            LOGGER.error(e.getMessage(), e);
            showDialog(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Failed to save PropertyColumns",
                    e,
                    result -> this.sqlgTreeDataFormNameTxt.setText(this.propertyColumnUI.getName())
            );
        } finally {
            getSqlgGraph().tx().rollback();
        }
    }
}
