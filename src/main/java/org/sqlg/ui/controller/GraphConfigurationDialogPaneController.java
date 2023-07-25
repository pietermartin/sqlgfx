package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.converter.DefaultStringConverter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.GraphConfiguration;
import org.sqlg.ui.model.GraphGroup;
import org.umlg.sqlg.structure.SqlgDataSource;
import org.umlg.sqlg.structure.SqlgDataSourceFactory;

import java.util.HashMap;

public class GraphConfigurationDialogPaneController {

    private final static Logger LOGGER = LoggerFactory.getLogger(GraphConfigurationDialogPaneController.class);
    private final GraphGroup graphGroup;
    private final LeftPaneController leftPaneController;
    private final ObservableList<GraphConfiguration> graphConfigurations;
    @FXML
    private DialogPane graphConfigurationDialogPane;
    @FXML
    private TextField nameTxt;
    @FXML
    private TextField urlTxt;
    @FXML
    private TextField usernameTxt;
    @FXML
    private TextField passwordTxt;
    @FXML
    private Button addGraphDialogTestButton;
    @FXML
    private ProgressBar addGraphDialogTestProgressBar;
    private final FontIcon noneChecked = new FontIcon(BoxiconsRegular.SQUARE);
    private final FontIcon check = new FontIcon(BoxiconsRegular.CHECK_SQUARE);
    private final FontIcon unchecked = new FontIcon(BoxiconsRegular.X);

    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty url = new SimpleStringProperty("");
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");

    public GraphConfigurationDialogPaneController(LeftPaneController leftPaneController, GraphGroup graphGroup, ObservableList<GraphConfiguration> graphConfigurations) {
        this.leftPaneController = leftPaneController;
        this.graphGroup = graphGroup;
        this.graphConfigurations = graphConfigurations;
    }

    @FXML
    public void initialize() {
        Dialog<ButtonType> addGraphDialog = new Dialog<>();
        addGraphDialog.setTitle("Add a graph");
        addGraphDialog.setDialogPane(this.graphConfigurationDialogPane);

        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        addGraphDialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        this.name.addListener((o, oldVal, newVal) -> {
        });
        this.url.addListener((o, oldVal, newVal) -> {
        });
        this.username.addListener((o, oldVal, newVal) -> {
        });
        this.password.addListener((o, oldVal, newVal) -> {
        });
        Bindings.bindBidirectional(this.nameTxt.textProperty(), this.name, new DefaultStringConverter());
        Bindings.bindBidirectional(this.usernameTxt.textProperty(), this.username, new DefaultStringConverter());
        Bindings.bindBidirectional(this.passwordTxt.textProperty(), this.password, new DefaultStringConverter());
        Bindings.bindBidirectional(this.urlTxt.textProperty(), this.url, new DefaultStringConverter());

        BooleanBinding booleanBinding = Bindings.createBooleanBinding(() ->
                        (this.nameTxt.getText().isEmpty() ||
                                this.urlTxt.getText().isEmpty() ||
                                this.username.get().isEmpty() ||
                                this.password.get().isEmpty()),
                this.nameTxt.textProperty(),
                this.urlTxt.textProperty(),
                this.usernameTxt.textProperty(),
                this.passwordTxt.textProperty()
        );
        addGraphDialog.getDialogPane().lookupButton(save).disableProperty().bind(booleanBinding);
        this.addGraphDialogTestButton.disableProperty().bind(booleanBinding);

        this.addGraphDialogTestButton.setGraphic(this.noneChecked);
        this.addGraphDialogTestButton.setContentDisplay(ContentDisplay.RIGHT);
        this.addGraphDialogTestButton.setOnAction(event -> {
            this.addGraphDialogTestProgressBar.styleProperty().set("");
            Task<FontIcon> connectionTask = new Task<>() {
                @Override
                protected FontIcon call() {
                    SqlgDataSource dataSource = null;
                    try {
                        Configuration configuration = new MapConfiguration(new HashMap<>() {{
                            put("jdbc.url", GraphConfigurationDialogPaneController.this.url.get());
                            put("jdbc.username", GraphConfigurationDialogPaneController.this.username.get());
                            put("jdbc.password", GraphConfigurationDialogPaneController.this.password.get());
                        }});
                        dataSource = SqlgDataSourceFactory.create(configuration);
                        updateProgress(ProgressBar.INDETERMINATE_PROGRESS, ProgressBar.INDETERMINATE_PROGRESS);
                        updateValue(noneChecked);
                        dataSource.getDatasource().getConnection();
                        updateValue(check);
                        GraphConfigurationDialogPaneController.this.addGraphDialogTestProgressBar.styleProperty().set("-fx-accent: green;");
                        return check;
                    } catch (Exception e) {
                        GraphConfigurationDialogPaneController.this.addGraphDialogTestProgressBar.styleProperty().set("-fx-accent: red;");
                        return unchecked;
                    } finally {
                        updateProgress(1D, 1D);
                        if (dataSource != null) {
                            dataSource.close();
                        }
                    }
                }
            };
            this.addGraphDialogTestButton.graphicProperty().bind(connectionTask.valueProperty());
            this.addGraphDialogTestProgressBar.progressProperty().bind(connectionTask.progressProperty());
            Thread.ofVirtual().start(connectionTask);
        });

        addGraphDialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                GraphConfiguration graphConfiguration = new GraphConfiguration(
                        this.leftPaneController,
                        this.graphGroup,
                        this.name.get(),
                        this.url.get(),
                        this.username.get(),
                        this.password.get(),
                        GraphConfiguration.TESTED.UNTESTED
                );
                this.graphConfigurations.add(graphConfiguration);
            }
        });
    }

}
