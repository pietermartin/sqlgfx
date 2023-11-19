package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;
import org.sqlg.ui.model.GraphConfiguration;

public class GraphConfigurationFormController extends BaseController {

    private final GraphConfiguration graphConfiguration;

    private final FontIcon noneChecked = new FontIcon(BoxiconsRegular.SQUARE);
    private final FontIcon check = new FontIcon(BoxiconsRegular.CHECK_SQUARE);
    private final FontIcon unchecked = new FontIcon(BoxiconsRegular.X);

    protected final VBox root;

    public GraphConfigurationFormController(Stage stage, GraphConfiguration graphConfiguration) {
        super(stage);
        this.graphConfiguration = graphConfiguration;
        this.root = new VBox(10);
        this.root.setPadding(new Insets(10, 10, 10, 10));
        this.root.setMaxHeight(Double.MAX_VALUE);
        initialize();
    }

    private void initialize() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        int rowIndex = 0;

        Label nameLabel = new Label("name");
        GridPane.setConstraints(nameLabel, 0, rowIndex);
        TextField nameTextField = new TextField(this.graphConfiguration.getName());
        GridPane.setConstraints(nameTextField, 1, rowIndex);
        Bindings.bindBidirectional(nameTextField.textProperty(), this.graphConfiguration.nameProperty());
        rowIndex++;

        Label urlLabel = new Label("url");
        GridPane.setConstraints(urlLabel, 0, rowIndex);
        TextField urlTextField = new TextField(this.graphConfiguration.getUrl());
        GridPane.setConstraints(urlTextField, 1, rowIndex);
        Bindings.bindBidirectional(urlTextField.textProperty(), this.graphConfiguration.urlProperty());

        rowIndex++;
        Label usernameLabel = new Label("username");
        GridPane.setConstraints(usernameLabel, 0, rowIndex);
        TextField usernameTextField = new TextField(this.graphConfiguration.getJdbcUser());
        GridPane.setConstraints(usernameTextField, 1, rowIndex);
        Bindings.bindBidirectional(usernameTextField.textProperty(), this.graphConfiguration.jdbcUserProperty());
        rowIndex++;

        Label savePasswordLabel = new Label("save password");
        GridPane.setConstraints(savePasswordLabel, 0, rowIndex);
        CheckBox savePasswordCheckBox = new CheckBox();
        Bindings.bindBidirectional(savePasswordCheckBox.selectedProperty(), this.graphConfiguration.savePasswordProperty());
        GridPane.setConstraints(savePasswordCheckBox, 1, rowIndex);

        rowIndex++;
        Label passwordLabel = new Label("password");
        GridPane.setConstraints(passwordLabel, 0, rowIndex);
        PasswordField passwordField = new PasswordField();
        BooleanBinding savePasswordBooleanBinding = Bindings.createBooleanBinding(
                () -> !savePasswordCheckBox.isSelected(),
                savePasswordCheckBox.selectedProperty()
        );
        passwordField.disableProperty().bind(savePasswordBooleanBinding);
        savePasswordCheckBox.selectedProperty().addListener((ignore1, ignore2, newValue) -> {
            if (!newValue) {
                passwordField.setText(null);
            }
        });

        GridPane.setConstraints(passwordField, 1, rowIndex);
        Bindings.bindBidirectional(passwordField.textProperty(), this.graphConfiguration.jdbcPasswordProperty());

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(80);
        gridPane.getColumnConstraints().addAll(column1, column2); // each get 50% of width
        gridPane.getChildren().addAll(
                nameLabel, nameTextField,
                urlLabel, urlTextField,
                usernameLabel, usernameTextField,
                savePasswordLabel, savePasswordCheckBox,
                passwordLabel, passwordField
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

        save.setOnAction(ignore -> save());
        cancel.setOnAction(ignore -> cancel());
        root.getChildren().add(vBox);
    }

    void cancel() {
        System.out.println("cancel");
    }

    void save() {
        this.graphConfiguration.getGraphGroup().getUser().getRoot().persistConfig();
    }

    public Parent getView() {
        return this.root;
    }
}
