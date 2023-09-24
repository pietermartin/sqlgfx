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
import org.sqlg.ui.model.ISqlgTopologyUI;
import org.sqlg.ui.model.PropertyColumnUI;
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
        this.propertyColumnUI.getPropertyColumn().remove();
    }

    @Override
    protected void rename() {
        SqlgGraph sqlgGraph = getSqlgGraph();
        try {
            this.propertyColumnUI.getPropertyColumn().rename(this.sqlgTreeDataFormNameTxt.getText());
            sqlgGraph.tx().commit();
            Platform.runLater(() -> this.propertyColumnUI.selectInTree(this.sqlgTreeDataFormNameTxt.getText()));
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
        gridPane.setHgap(5);
        gridPane.setVgap(5);

        Label propertyTypeLabel = new Label("PropertyType");
        GridPane.setConstraints(propertyTypeLabel, 0, 0);

        this.propertyTypeCombobox = new ComboBox<>();
        this.propertyTypeCombobox.getItems().addAll(PropertyType.values());
        this.propertyTypeCombobox.setPrefWidth(Double.MAX_VALUE);
        this.propertyTypeCombobox.setValue(PropertyType.STRING);
        this.propertyTypeCombobox.setDisable(true);
        GridPane.setConstraints(this.propertyTypeCombobox, 1, 0);

        Label lowerLabel = new Label("lower");
        GridPane.setConstraints(lowerLabel, 0, 1);
        this.lowerTextField = new TextField(Long.toString(propertyColumnUI.getLower()));
        this.lowerTextField.setTextFormatter(new TextFormatter<>(filter));
        this.lowerTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        StringConverter<? extends Number> converter = new LongStringConverter();
        Bindings.bindBidirectional(this.lowerTextField.textProperty(), propertyColumnUI.lowerProperty(), (StringConverter) converter);

        GridPane.setConstraints(this.lowerTextField, 1, 1);

        Label upperLabel = new Label("upper");
        GridPane.setConstraints(upperLabel, 0, 2);
        this.upperTextField = new TextField(Long.toString(propertyColumnUI.getUpper()));
        this.upperTextField.setTextFormatter(new TextFormatter<>(filter));
        Bindings.bindBidirectional(this.upperTextField.textProperty(), propertyColumnUI.upperProperty(), (StringConverter) converter);

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
        Bindings.bindBidirectional(this.defaultLiteralTextField.textProperty(), propertyColumnUI.defaultLiteralProperty());
        GridPane.setConstraints(this.defaultLiteralTextField, 1, 5);

        Label checkConstraintLabel = new Label("checkConstraint");
        GridPane.setConstraints(checkConstraintLabel, 0, 6);
        this.checkConstraintTextField = new TextField(propertyColumnUI.getCheckConstraint());
        this.checkConstraintTextField.disableProperty().bind(Bindings.createBooleanBinding(() -> !editToggleSwitch.isSelected(), editToggleSwitch.selectedProperty()));
        Bindings.bindBidirectional(this.checkConstraintTextField.textProperty(), propertyColumnUI.checkConstraintProperty());
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
        save.setOnAction((event) -> {
            save();
        });
        cancel.setOnAction((event) -> {
            cancel();
        });
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
