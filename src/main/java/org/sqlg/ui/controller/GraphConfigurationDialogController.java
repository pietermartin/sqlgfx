package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.sqlg.ui.App;
import org.sqlg.ui.Fontawesome;
import org.sqlg.ui.model.GraphGroup;
import org.umlg.sqlg.structure.SqlgDataSource;
import org.umlg.sqlg.structure.SqlgDataSourceFactory;

import java.util.HashMap;

import static org.sqlg.ui.Fontawesome.Type.Regular;

public class GraphConfigurationDialogController extends BaseController {

    private final GraphGroup graphGroup;

    private final Label noneChecked = Fontawesome.SQUARE.label(Regular);
    private final Label check = Fontawesome.SQUARE_XMARK.label(Regular);
    private final Label unchecked = Fontawesome.XMARK.label(Regular);

    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty url = new SimpleStringProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final BooleanProperty savePassword = new SimpleBooleanProperty();
    private final StringProperty password = new SimpleStringProperty();

    public GraphConfigurationDialogController(Stage stage, GraphGroup graphGroup) {
        super(stage);
        this.graphGroup = graphGroup;
        initialize();
    }

    private void initialize() {
        DialogPane graphConfigurationDialogPane = new DialogPane();
        graphConfigurationDialogPane.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        graphConfigurationDialogPane.setMinWidth(630);
        graphConfigurationDialogPane.setMinHeight(195);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setAlignment(Pos.CENTER);
        graphConfigurationDialogPane.setContent(gridPane);

        int rowIndex = 0;
        Label nameLabel = new Label("name");
        GridPane.setConstraints(nameLabel, 0, rowIndex);
        TextField nameTxt = new TextField("");
        Bindings.bindBidirectional(nameTxt.textProperty(), this.name);
        GridPane.setConstraints(nameTxt, 1, rowIndex);
        rowIndex++;

        Label urlLabel = new Label("url");
        GridPane.setConstraints(urlLabel, 0, rowIndex);
        TextField urlTxt = new TextField("");
        Bindings.bindBidirectional(urlTxt.textProperty(), this.url);
        GridPane.setConstraints(urlTxt, 1, rowIndex);
        rowIndex++;

        Label usernameLabel = new Label("username");
        GridPane.setConstraints(usernameLabel, 0, rowIndex);
        TextField usernameTxt = new TextField("");
        Bindings.bindBidirectional(usernameTxt.textProperty(), this.username);
        GridPane.setConstraints(usernameTxt, 1, rowIndex);
        rowIndex++;

        Label savePasswordLabel = new Label("save password");
        GridPane.setConstraints(savePasswordLabel, 0, rowIndex);
        CheckBox savePasswordCheckBox = new CheckBox();
        Bindings.bindBidirectional(savePasswordCheckBox.selectedProperty(), this.savePassword);
        GridPane.setConstraints(savePasswordCheckBox, 1, rowIndex);
        rowIndex++;

        Label passwordLabel = new Label("password");
        GridPane.setConstraints(passwordLabel, 0, rowIndex);
        PasswordField passwordTxt = new PasswordField();
        passwordTxt.setDisable(true);
        BooleanBinding savePasswordBooleanBinding = Bindings.createBooleanBinding(
                () -> !savePasswordCheckBox.isSelected(),
                savePasswordCheckBox.selectedProperty()
        );
        passwordTxt.disableProperty().bind(savePasswordBooleanBinding);
        Bindings.bindBidirectional(passwordTxt.textProperty(), this.password);
        GridPane.setConstraints(passwordTxt, 1, rowIndex);
        rowIndex++;

        Button addGraphDialogTestButton = new Button("test");
        GridPane.setConstraints(addGraphDialogTestButton, 0, rowIndex);
        ProgressBar addGraphDialogTestProgressBar = new ProgressBar();
        GridPane.setConstraints(addGraphDialogTestProgressBar, 1, rowIndex);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(20);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(80);
        gridPane.getColumnConstraints().addAll(column1, column2);
        gridPane.getChildren().addAll(
                nameLabel, nameTxt,
                urlLabel, urlTxt,
                usernameLabel, usernameTxt,
                savePasswordLabel, savePasswordCheckBox,
                passwordLabel, passwordTxt,
                addGraphDialogTestButton, addGraphDialogTestProgressBar
        );

        Dialog<ButtonType> addGraphDialog = new Dialog<>();
        addGraphDialog.setTitle("Add a graph");
        addGraphDialog.setDialogPane(graphConfigurationDialogPane);

        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        addGraphDialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        Bindings.bindBidirectional(nameTxt.textProperty(), this.name, new DefaultStringConverter());
        Bindings.bindBidirectional(usernameTxt.textProperty(), this.username, new DefaultStringConverter());
        Bindings.bindBidirectional(passwordTxt.textProperty(), this.password, new DefaultStringConverter());
        Bindings.bindBidirectional(urlTxt.textProperty(), this.url, new DefaultStringConverter());

        BooleanBinding booleanBinding = Bindings.createBooleanBinding(() ->
                        (nameTxt.getText().isEmpty() ||
                                urlTxt.getText().isEmpty() ||
                                username.get().isEmpty()),
                nameTxt.textProperty(),
                urlTxt.textProperty(),
                usernameTxt.textProperty()
        );
        addGraphDialog.getDialogPane().lookupButton(save).disableProperty().bind(booleanBinding);
        addGraphDialogTestButton.disableProperty().bind(booleanBinding);

//        addGraphDialogTestButton.setAlignment(Pos.TOP_CENTER);    aa
        addGraphDialogTestButton.setGraphic(this.noneChecked);

        addGraphDialogTestButton.setContentDisplay(ContentDisplay.RIGHT);
        addGraphDialogTestButton.setOnAction(ignore -> {
            addGraphDialogTestProgressBar.styleProperty().set("");
            Task<Label> connectionTask = new Task<>() {
                @Override
                protected Label call() {
                    SqlgDataSource dataSource = null;
                    try {
                        Configuration configuration = new MapConfiguration(new HashMap<>() {{
                            put("jdbc.url", url.get());
                            put("jdbc.username", username.get());
                            put("jdbc.password", password.get());
                        }});
                        dataSource = SqlgDataSourceFactory.create(configuration);
                        updateProgress(ProgressBar.INDETERMINATE_PROGRESS, ProgressBar.INDETERMINATE_PROGRESS);
                        updateValue(noneChecked);
                        dataSource.getDatasource().getConnection();
                        updateValue(check);
                        addGraphDialogTestProgressBar.styleProperty().set("-fx-accent: green;");
                        return check;
                    } catch (Exception e) {
                        addGraphDialogTestProgressBar.styleProperty().set("-fx-accent: red;");
                        return unchecked;
                    } finally {
                        updateProgress(1D, 1D);
                        if (dataSource != null) {
                            dataSource.close();
                        }
                    }
                }
            };
            addGraphDialogTestButton.graphicProperty().bind(connectionTask.valueProperty());
            addGraphDialogTestProgressBar.progressProperty().bind(connectionTask.progressProperty());
            Thread.ofVirtual().start(connectionTask);
        });

        addGraphDialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                this.graphGroup.add(
                        this.name.get(),
                        this.url.get(),
                        this.username.get(),
                        this.savePassword.get(),
                        this.password.get()
                );
            }
        });
    }
}
