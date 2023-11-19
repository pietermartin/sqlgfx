package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.sqlg.ui.model.GraphGroup;
import org.sqlg.ui.model.Root;
import org.sqlg.ui.model.User;

import java.util.Optional;

public class LoginFormController extends BaseController {

    private final Root root;
    private final BorderPane borderPane;
    private final SimpleStringProperty username = new SimpleStringProperty();
    private final SimpleStringProperty password = new SimpleStringProperty();
    private final PrimaryController primaryController;

    public LoginFormController(Stage stage, Root root, BorderPane borderPane, PrimaryController primaryController) {
        super(stage);
        this.root = root;
        this.borderPane = borderPane;
        this.primaryController = primaryController;
    }

    public void initialise() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);

        HBox outerHBox = new HBox(10);
        outerHBox.setPadding(new Insets(10, 10, 10, 10));
        outerHBox.setMaxHeight(Double.MAX_VALUE);
        this.borderPane.setCenter(outerHBox);

        int rowIndex = 0;

        Label nameLabel = new Label("username");
        GridPane.setConstraints(nameLabel, 0, rowIndex);
        TextField nameTextField = new TextField();
        Platform.runLater(nameTextField::requestFocus);
        GridPane.setConstraints(nameTextField, 1, rowIndex);
        Bindings.bindBidirectional(nameTextField.textProperty(), this.username);
        rowIndex++;

        Label passwordLabel = new Label("password");
        GridPane.setConstraints(passwordLabel, 0, rowIndex);
        PasswordField passwordField = new PasswordField();
        GridPane.setConstraints(passwordField, 1, rowIndex);
        Bindings.bindBidirectional(passwordField.textProperty(), this.password);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(30);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(70);
        gridPane.getColumnConstraints().addAll(column1, column2); // each get 50% of width
        gridPane.getChildren().addAll(
                nameLabel, nameTextField,
                passwordLabel, passwordField
        );
        VBox.setVgrow(gridPane, Priority.ALWAYS);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setPadding(new Insets(0, 5, 0, 0));
        Button login = new Button("Login");
        buttonBar.getButtons().addAll(login);

        VBox vBox = new VBox(5, gridPane, buttonBar);
        vBox.setPadding(new Insets(0, 0, 5, 0));
        vBox.setAlignment(Pos.CENTER);

        VBox.setVgrow(gridPane, Priority.NEVER);
        VBox.setVgrow(buttonBar, Priority.NEVER);

        login.setOnAction(ignore -> {
            try {
                login();
            } catch (Exception e) {
                showDialog(
                        Alert.AlertType.ERROR,
                        "Login failure",
                        "Invalid username or password!"
                );
            }
        });

//        this.outerHBox.setStyle("-fx-background-color: red;");
//        vBox.setStyle("-fx-background-color: blue;");
        outerHBox.getChildren().add(vBox);
        outerHBox.setAlignment(Pos.CENTER);
    }

    void login() {
        Optional<User> userOpt = root.getUsers().stream().filter(u -> u.getUsername().equals(this.username.get())).findAny();
        if (userOpt.isPresent()) {
            userOpt.get().setPassword(this.password.get());
            this.primaryController.log(userOpt.get());
        } else {
            User user = new User(this.root, this.username.get(), this.password.get());
            this.root.getUsers().add(user);
            user.getGraphGroups().add(new GraphGroup(user, "default"));
            this.primaryController.log(user);
        }
    }
}
