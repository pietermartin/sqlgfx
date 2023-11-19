package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import org.sqlg.ui.GraphConfigurationTreeItem;
import org.sqlg.ui.model.GraphConfiguration;

public class DatabasePasswordDialogController extends BaseController {

    private final GraphConfigurationTreeItem graphConfigurationTreeItem;
    private final BooleanProperty savePassword = new SimpleBooleanProperty();
    private final StringProperty password = new SimpleStringProperty();

    public DatabasePasswordDialogController(
            Stage stage,
            GraphConfigurationTreeItem graphConfigurationTreeItem) {

        super(stage);
        this.graphConfigurationTreeItem = graphConfigurationTreeItem;
        initialize();
    }

    private void initialize() {

        DialogPane graphConfigurationDialogPane = new DialogPane();
        graphConfigurationDialogPane.setMinWidth(630);
        graphConfigurationDialogPane.setMinHeight(195);
//        this.graphConfigurationDialogPane.setStyle("-fx-background-color: red;");
        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setAlignment(Pos.CENTER);
        graphConfigurationDialogPane.setContent(gridPane);

        int rowIndex = 0;
        Label savePasswordLabel = new Label("save password");
        GridPane.setConstraints(savePasswordLabel, 0, rowIndex);
        CheckBox savePasswordCheckBox = new CheckBox();
        Bindings.bindBidirectional(savePasswordCheckBox.selectedProperty(), this.savePassword);
        GridPane.setConstraints(savePasswordCheckBox, 1, rowIndex);

        rowIndex++;
        Label passwordLabel = new Label("password");
        GridPane.setConstraints(passwordLabel, 0, rowIndex);
        PasswordField passwordTxt = new PasswordField();
        passwordTxt.setDisable(false);
        Bindings.bindBidirectional(passwordTxt.textProperty(), this.password);
        GridPane.setConstraints(passwordTxt, 1, rowIndex);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(80);
        gridPane.getColumnConstraints().addAll(column1, column2);
        gridPane.getChildren().addAll(
                savePasswordLabel, savePasswordCheckBox,
                passwordLabel, passwordTxt
        );

        Dialog<ButtonType> addGraphDialog = new Dialog<>();
        addGraphDialog.setTitle("Password!");
        addGraphDialog.setDialogPane(graphConfigurationDialogPane);

        ButtonType connect = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        addGraphDialog.getDialogPane().getButtonTypes().addAll(connect, ButtonType.CANCEL);

        Bindings.bindBidirectional(passwordTxt.textProperty(), this.password, new DefaultStringConverter());

        BooleanBinding booleanBinding = Bindings.createBooleanBinding(() ->
                        passwordTxt.getText().isEmpty(),
                passwordTxt.textProperty()
        );
        addGraphDialog.getDialogPane().lookupButton(connect).disableProperty().bind(booleanBinding);

        addGraphDialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                GraphConfiguration graphConfiguration = (GraphConfiguration) this.graphConfigurationTreeItem.getValue();
                graphConfiguration.setSavePassword(this.savePassword.get());
                graphConfiguration.setJdbcPassword(this.password.get());
                if (this.savePassword.get()) {
                    graphConfiguration.getGraphGroup().getUser().getRoot().persistConfig();
                }
                this.graphConfigurationTreeItem.openGraphConfigurationTreeItem(graphConfiguration);
            }
        });
    }
}
