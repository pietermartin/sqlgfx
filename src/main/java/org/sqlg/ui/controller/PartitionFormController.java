package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.ISqlgTopologyUI;
import org.sqlg.ui.model.PartitionUI;
import org.umlg.sqlg.structure.SqlgGraph;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class PartitionFormController extends BaseNameFormController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartitionFormController.class);
    private final PartitionUI partitionUI;

    public PartitionFormController(LeftPaneController leftPaneController, PartitionUI partitionUI) {
        super(leftPaneController, partitionUI);
        this.partitionUI = partitionUI;
        this.sqlgTreeDataFormNameTxt.disableProperty().unbind();
        this.sqlgTreeDataFormNameTxt.setDisable(true);
    }

    @Override
    protected SqlgGraph getSqlgGraph() {
        return this.partitionUI.getSqlgGraph();
    }

    @Override
    protected void delete() {
        this.partitionUI.getPartition().remove();
    }

    @Override
    protected void rename() {
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

        PartitionUI partitionUI = (PartitionUI) sqlgTopologyUI;

        HBox partitionTypeHBox = new HBox(5);
        TextField partitionTypeTextField = new TextField(partitionUI.getPartitionType());
        partitionTypeTextField.setEditable(false);
        partitionTypeTextField.setDisable(true);
        Label partitionTypeLabel = new Label("partitionType");
        partitionTypeLabel.setMinWidth(TOP_LABEL_MIN_WIDTH);
        partitionTypeHBox.setAlignment(Pos.CENTER_LEFT);
        partitionTypeHBox.getChildren().addAll(partitionTypeLabel, partitionTypeTextField);

        HBox partitionExpressionHBox = new HBox(5);
        TextField partitionExpressionTextField = new TextField(partitionUI.getPartitionExpression());
        partitionExpressionTextField.setEditable(false);
        partitionExpressionTextField.setDisable(true);
        Label partitionExpresssionLabel = new Label("partitionExpression");
        partitionExpresssionLabel.setMinWidth(TOP_LABEL_MIN_WIDTH);
        partitionExpressionHBox.setAlignment(Pos.CENTER_LEFT);
        partitionExpressionHBox.getChildren().addAll(partitionExpresssionLabel, partitionExpressionTextField);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(0, 5, 0, 5));
        int rowIndex = 0;

        Label propertyTypeLabel = new Label("propertyType");
        GridPane.setConstraints(propertyTypeLabel, 0, rowIndex);
        TextField propertyTypeTextField = new TextField(partitionUI.getPartitionType());
        propertyTypeTextField.setDisable(true);
        GridPane.setConstraints(propertyTypeTextField, 1, rowIndex);
        rowIndex++;

        Label propertyExpressionLabel = new Label("propertyExpression");
        GridPane.setConstraints(propertyExpressionLabel, 0, rowIndex);
        TextField propertyExpressionTextField = new TextField(partitionUI.getPartitionExpression());
        propertyExpressionTextField.setDisable(true);
        GridPane.setConstraints(propertyExpressionTextField, 1, rowIndex);
        rowIndex++;

        Label fromLabel = new Label("from");
        GridPane.setConstraints(fromLabel, 0, rowIndex);
        TextField fromTextField = new TextField(partitionUI.getFrom());
        fromTextField.setDisable(true);
        GridPane.setConstraints(fromTextField, 1, rowIndex);
        rowIndex++;

        Label toLabel = new Label("to");
        GridPane.setConstraints(toLabel, 0, rowIndex);
        TextField toTextField = new TextField(partitionUI.getTo());
        toTextField.setDisable(true);
        GridPane.setConstraints(toTextField, 1, rowIndex);
        rowIndex++;

        Label inLabel = new Label("in");
        GridPane.setConstraints(inLabel, 0, rowIndex);
        TextField inTextField = new TextField(partitionUI.getIn());
        inTextField.setDisable(true);
        GridPane.setConstraints(inTextField, 1, rowIndex);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(80);
        gridPane.getColumnConstraints().addAll(column1, column2); // each get 50% of width
        gridPane.getChildren().addAll(
                fromLabel, fromTextField,
                toLabel, toTextField,
                inLabel, inTextField,
                propertyTypeLabel, propertyTypeTextField,
                propertyExpressionLabel, propertyExpressionTextField
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
        save.setOnAction(ignore -> {
            save();
        });
        cancel.setOnAction(ignore -> {
            cancel();
        });
        return List.of(vBox);
    }

    void cancel() {
//        this.propertyColumnUI.reset();
        this.getSqlgGraph().tx().rollback();
    }

    void save() {
        try {
//            if (this.propertyColumnUI.getLower() != this.propertyColumnUI.getPropertyColumn().getPropertyDefinition().multiplicity().lower() ||
//                            this.propertyColumnUI.getUpper() != this.propertyColumnUI.getPropertyColumn().getPropertyDefinition().multiplicity().upper() ||
//                            !this.propertyColumnUI.getDefaultLiteral().equals(this.propertyColumnUI.getPropertyColumn().getPropertyDefinition().defaultLiteral()) ||
//                            !this.propertyColumnUI.getCheckConstraint().equals(this.propertyColumnUI.getPropertyColumn().getPropertyDefinition().checkConstraint())
//            ) {
//                PropertyDefinition updatedPropertyDefinition = PropertyDefinition.of(
//                        PropertyType.valueOf(this.propertyColumnUI.getPropertyType()),
//                        Multiplicity.of(
//                                this.propertyColumnUI.getLower(),
//                                this.propertyColumnUI.getUpper(),
//                                this.propertyColumnUI.isUnique()
//                        ),
//                        this.propertyColumnUI.getDefaultLiteral(),
//                        this.propertyColumnUI.getCheckConstraint()
//                );
//                AbstractLabel abstractLabel = this.propertyColumnUI.getPropertyColumn().getParentLabel();
//                String propertyName = this.propertyColumnUI.getPropertyColumn().getName();
//                this.propertyColumnUI.getPropertyColumn().updatePropertyDefinition(updatedPropertyDefinition);
//                this.propertyColumnUI.setPropertyColumn(abstractLabel.getProperty(propertyName).orElseThrow());
//            }
            getSqlgGraph().tx().commit();
            showDialog(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Saved PropertyColumns"
            );
        } catch (Exception e) {
            getSqlgGraph().tx().rollback();
            LOGGER.error(e.getMessage(), e);
//            showDialog(
//                    Alert.AlertType.ERROR,
//                    "Error",
//                    "Failed to save PropertyColumns",
//                    e,
//                    result -> this.sqlgTreeDataFormNameTxt.setText(this.propertyColumnUI.getName())
//            );
        } finally {
            getSqlgGraph().tx().rollback();
        }
    }
}
