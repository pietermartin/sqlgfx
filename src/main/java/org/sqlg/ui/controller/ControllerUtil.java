package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.tableview2.cell.ComboBox2TableCell;
import org.controlsfx.control.tableview2.cell.TextField2TableCell;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.TopologyTreeItem;
import org.sqlg.ui.model.IndexUI;
import org.sqlg.ui.model.PropertyColumnUI;
import org.umlg.sqlg.structure.Multiplicity;
import org.umlg.sqlg.structure.PropertyDefinition;
import org.umlg.sqlg.structure.PropertyType;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.AbstractLabel;
import org.umlg.sqlg.structure.topology.IndexType;
import org.umlg.sqlg.structure.topology.PropertyColumn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ControllerUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerUtil.class);

    public static <R> void savePropertyColumns(
            SqlgGraph sqlgGraph,
            AbstractLabel abstractLabel,
            ObservableList<PropertyColumnUI> items,
            Callback<Boolean, R> onSuccess,
            Callback<Exception, R> onFailure
    ) {
        try {
            Map<String, PropertyColumnUI> deleted = new HashMap<>();
            Map<String, PropertyColumnUI> changed = new HashMap<>();
            for (PropertyColumnUI propertyColumnUI : items) {
                if (propertyColumnUI.isDelete()) {
                    propertyColumnUI.getPropertyColumn().remove();
                    deleted.put(propertyColumnUI.getName(), propertyColumnUI);
                } else if (!propertyColumnUI.getName().equals(propertyColumnUI.getPropertyColumn().getName())) {
                    propertyColumnUI.getPropertyColumn().rename(propertyColumnUI.getName());
                    changed.put(propertyColumnUI.getName(), propertyColumnUI);
                }
                if (!propertyColumnUI.isDelete() &&
                        (propertyColumnUI.getLower() != propertyColumnUI.getPropertyColumn().getPropertyDefinition().multiplicity().lower() ||
                                propertyColumnUI.getUpper() != propertyColumnUI.getPropertyColumn().getPropertyDefinition().multiplicity().upper() ||
                                !propertyColumnUI.getDefaultLiteral().equals(propertyColumnUI.getPropertyColumn().getPropertyDefinition().defaultLiteral()) ||
                                !propertyColumnUI.getCheckConstraint().equals(propertyColumnUI.getPropertyColumn().getPropertyDefinition().checkConstraint()))
                ) {
                    PropertyDefinition updatedPropertyDefinition = PropertyDefinition.of(
                            PropertyType.valueOf(propertyColumnUI.getPropertyType()),
                            Multiplicity.of(
                                    propertyColumnUI.getLower(),
                                    propertyColumnUI.getUpper(),
                                    propertyColumnUI.isUnique()
                            ),
                            propertyColumnUI.getDefaultLiteral(),
                            propertyColumnUI.getCheckConstraint()
                    );
                    propertyColumnUI.getPropertyColumn().updatePropertyDefinition(updatedPropertyDefinition);
                    changed.put(propertyColumnUI.getName(), propertyColumnUI);
                }
            }
            sqlgGraph.tx().commit();
            for (PropertyColumnUI propertyColumnUI : changed.values()) {
                PropertyColumn changedPropertyColumn = abstractLabel.getProperty(propertyColumnUI.getName()).orElseThrow();
                propertyColumnUI.setPropertyColumn(changedPropertyColumn);
            }
            for (String name : deleted.keySet()) {
                PropertyColumnUI deletedPropertyColumnUI = deleted.get(name);
                items.remove(deletedPropertyColumnUI);
            }
            onSuccess.call(true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            onFailure.call(e);
        } finally {
            sqlgGraph.tx().rollback();
        }
    }

    public static Node propertyColumnsTableView(
            ObservableList<PropertyColumnUI> propertyColumnUIs,
            BooleanProperty editableProperty,
            EventHandler<ActionEvent> saveAction,
            EventHandler<ActionEvent> cancelAction
    ) {
        TableView<PropertyColumnUI> tableView = new TableView<>();
        tableView.editableProperty().bind(editableProperty);
        tableView.setFixedCellSize(30D);

        TableColumn<PropertyColumnUI, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        //PropertyDefinition start
        TableColumn<PropertyColumnUI, String> propertyTypeColumn = new TableColumn<>("propertyType");
        propertyTypeColumn.setCellFactory(ComboBox2TableCell.forTableColumn(Arrays.stream(PropertyType.values()).map(PropertyType::name).toList().toArray(new String[]{})));
        propertyTypeColumn.setCellValueFactory(p -> p.getValue().propertyTypeProperty());
        propertyTypeColumn.setEditable(false);

        //Multiplicity start
        TableColumn<PropertyColumnUI, Long> lowerColumn = new TableColumn<>("lower");
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
        TableColumn<PropertyColumnUI, Long> upperColumn = new TableColumn<>("upper");
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

        TableColumn<PropertyColumnUI, Boolean> uniqueColumn = new TableColumn<>("unique");
        uniqueColumn.setCellValueFactory(p -> p.getValue().uniqueProperty().asObject());
        TableColumn<PropertyColumnUI, Boolean> orderedColumn = new TableColumn<>("ordered");
        orderedColumn.setCellValueFactory(p -> p.getValue().orderedProperty().asObject());
        //Multiplicity end
        TableColumn<PropertyColumnUI, ?> multiplicityColumn = new TableColumn<>("Multiplicity");
        multiplicityColumn.getColumns().addAll(lowerColumn, upperColumn, uniqueColumn, orderedColumn);

        TableColumn<PropertyColumnUI, String> defaultLiteralColumn = new TableColumn<>("defaultLiteral");
        defaultLiteralColumn.setCellValueFactory(p -> p.getValue().defaultLiteralProperty());
        defaultLiteralColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn<PropertyColumnUI, String> checkConstraintColumn = new TableColumn<>("checkConstraint");
        checkConstraintColumn.setCellValueFactory(p -> p.getValue().checkConstraintProperty());
        checkConstraintColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        //PropertyDefinition end

        TableColumn<PropertyColumnUI, Boolean> delete = new TableColumn<>("delete");
        delete.setText("delete");
        delete.setCellValueFactory(p -> p.getValue().deleteProperty());
        delete.setCellFactory(CheckBoxTableCell.forTableColumn(delete));
        delete.setPrefWidth(60);

        checkConstraintColumn.setCellValueFactory(p -> p.getValue().checkConstraintProperty());
        TableColumn<PropertyColumnUI, ?> propertyDefinitionColumn = new TableColumn<>("PropertyDefinition");
        propertyDefinitionColumn.getColumns().addAll(propertyTypeColumn, multiplicityColumn, defaultLiteralColumn, checkConstraintColumn);

        tableView.getColumns().addAll(nameColumn, propertyDefinitionColumn, delete);

        tableView.setItems(propertyColumnUIs);

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

        save.disableProperty().bind(Bindings.createBooleanBinding(() -> !editableProperty.get(), editableProperty));
        save.setOnAction(saveAction);
        cancel.setOnAction(cancelAction);
        return new TitledPane(TopologyTreeItem.PROPERTY_COLUMNS, vBox);
    }


    public static Node indexesTableView(
            ObservableList<IndexUI> indexUIs,
            BooleanProperty editableProperty,
            EventHandler<ActionEvent> saveAction,
            EventHandler<ActionEvent> cancelAction
    ) {
        TableView<IndexUI> tableView = new TableView<>();
        tableView.editableProperty().bind(editableProperty);
        tableView.setFixedCellSize(30D);

        TableColumn<IndexUI, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<IndexUI, String> indexTypeColumn = new TableColumn<>("indexType");
        indexTypeColumn.setCellFactory(ComboBox2TableCell.forTableColumn(IndexType.UNIQUE.getName()));
//        indexTypeColumn.setCellValueFactory(p -> p.getValue().getName());
        indexTypeColumn.setEditable(false);

        TableColumn<IndexUI, Boolean> delete = new TableColumn<>("delete");
        delete.setText("delete");
        delete.setCellValueFactory(p -> p.getValue().deleteProperty());
        delete.setCellFactory(CheckBoxTableCell.forTableColumn(delete));
        delete.setPrefWidth(60);

        tableView.getColumns().addAll(nameColumn, indexTypeColumn, delete);

        tableView.setItems(indexUIs);

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

        save.disableProperty().bind(Bindings.createBooleanBinding(() -> !editableProperty.get(), editableProperty));
        save.setOnAction(saveAction);
        cancel.setOnAction(cancelAction);

        return new TitledPane("Indexes", vBox);
    }

    public static void showDialog(Stage stage, Alert.AlertType alertType, String headerText, String text) {
        showDialog(stage, alertType, headerText, text, null, null);
    }

    public static void showDialog(
            Stage stage,
            Alert.AlertType alertType,
            String headerText,
            String text,
            Exception e,
            Consumer<Object> onClose) {

        Dialog<?> alert;
        if (alertType == Alert.AlertType.ERROR && e != null) {
            alert = new ExceptionDialog(e);
        } else {
            alert = new Alert(alertType, text);
        }
        alert.initModality(Modality.NONE);
        alert.initOwner(stage);
        alert.getDialogPane().setHeaderText(headerText);
        alert.getDialogPane().setMinWidth(400);
        alert.getDialogPane().setMinHeight(200);
        alert.setX(stage.getX() + stage.getWidth() - 410);
        alert.setY(stage.getY() + 10);
        if (alertType != Alert.AlertType.ERROR) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                Platform.runLater(alert::close);
            }).start();
        }
        if (onClose != null) {
            alert.showAndWait().ifPresent(onClose);
        } else {
            alert.showAndWait();
        }
    }
}
