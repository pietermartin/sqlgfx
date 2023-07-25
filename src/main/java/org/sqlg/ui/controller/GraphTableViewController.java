package org.sqlg.ui.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import net.synedra.validatorfx.Validator;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;
import org.sqlg.ui.App;
import org.sqlg.ui.model.GraphConfiguration;
import org.sqlg.ui.model.GraphGroup;

import java.io.IOException;

public class GraphTableViewController {

    private final GraphGroup graphGroup;
    private final LeftPaneController leftPaneController;
    private final ObservableList<GraphConfiguration> graphConfigurations;
    private GraphConfiguration selectedGraphConfiguration;
    @FXML
    private Button deleteGraphButton;
    @FXML
    private Button addGraphButton;
    @FXML
    private TableView<GraphConfiguration> graphTableView;
    private final Validator validator = new Validator();

    public GraphTableViewController(LeftPaneController leftPaneController, GraphGroup graphGroup, ObservableList<GraphConfiguration> graphConfigurations) {
        this.leftPaneController = leftPaneController;
        this.graphGroup = graphGroup;
        this.graphConfigurations = graphConfigurations;
    }

    @FXML
    protected void initialize() {
        this.graphTableView.setItems(this.graphConfigurations);
        this.graphTableView.setEditable(true);
        this.deleteGraphButton.setDisable(true);

        TableColumn<GraphConfiguration, String> graphNameCol = new TableColumn<>("name");
        graphNameCol.setCellValueFactory(new PropertyValueFactory<>(this.graphConfigurations.get(0).nameProperty().getName()));
        graphNameCol.setCellFactory(new Callback<>() {
            @Override
            public TextFieldTableCell<GraphConfiguration, String> call(TableColumn<GraphConfiguration, String> param) {
                TextFieldTableCell<GraphConfiguration, String> textFieldTableCell = new TextFieldTableCell<>(new DefaultStringConverter()) {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                    }
                };
                validator.createCheck()
                        .dependsOn("graphName", textFieldTableCell.textProperty())
                        .withMethod(c -> {
                            String graph = c.get("graphName");
                            if (graph != null && !graph.toLowerCase().equals(graph)) {
                                c.error("Please use only lowercase letters.");
                            }
                        })
                        .decorates(textFieldTableCell)
                        .immediate();
                ;
                return textFieldTableCell;
            }
        });
        graphNameCol.setOnEditCommit(event -> {
            GraphConfiguration graphConfiguration = event.getRowValue();
            final String value = event.getNewValue() != null ? event.getNewValue() : event.getOldValue();
            graphConfiguration.setName(value);
        });

        TableColumn<GraphConfiguration, String> urlCol = new TableColumn<>("url");
        urlCol.setCellValueFactory(new PropertyValueFactory<>(this.graphConfigurations.get(0).urlProperty().getName()));
        TableColumn<GraphConfiguration, String> usernameCol = new TableColumn<>("username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>(this.graphConfigurations.get(0).usernameProperty().getName()));
        TableColumn<GraphConfiguration, String> passwordCol = new TableColumn<>("password");
        passwordCol.setCellValueFactory(new PropertyValueFactory<>(this.graphConfigurations.get(0).passwordProperty().getName()));
        TableColumn<GraphConfiguration, GraphConfiguration.TESTED> testedCol = new TableColumn<>("");
        testedCol.setCellValueFactory(new PropertyValueFactory<>(this.graphConfigurations.get(0).testedProperty().getName()));

        this.graphTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.selectedGraphConfiguration = newValue;
                this.deleteGraphButton.setDisable(false);
            } else {
                this.deleteGraphButton.setDisable(true);
            }
        });

        this.addGraphButton.setOnAction(event -> {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("graphConfigurationDialogPane.fxml"));
            try {
                fxmlLoader.setControllerFactory(controllerClass -> new GraphConfigurationDialogPaneController(
                        this.leftPaneController,
                        this.graphGroup,
                        this.graphConfigurations
                ));
                fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        this.deleteGraphButton.setOnAction(event -> {
            this.graphConfigurations.remove(this.selectedGraphConfiguration);
        });


        Callback<TableColumn<GraphConfiguration, GraphConfiguration.TESTED>, TableCell<GraphConfiguration, GraphConfiguration.TESTED>> cellFactory = new Callback<>() {
            @Override
            public TableCell<GraphConfiguration, GraphConfiguration.TESTED> call(final TableColumn<GraphConfiguration, GraphConfiguration.TESTED> param) {
                return new TableCell<>() {

                    final FontIcon noneChecked = new FontIcon(BoxiconsRegular.SQUARE);
                    final FontIcon check = new FontIcon(BoxiconsRegular.CHECK_SQUARE);
                    final FontIcon unchecked = new FontIcon(BoxiconsRegular.X);
                    final Button btn = new Button("test", noneChecked);
                    final ProgressBar progressBar = new ProgressBar(0D);
                    final HBox buttonAndProgressBar = new HBox(5, btn, progressBar);

                    @Override
                    public void updateItem(GraphConfiguration.TESTED item, boolean empty) {
                        super.updateItem(item, empty);

                        final int FONT_SIZE = 16;
                        noneChecked.iconSizeProperty().set(FONT_SIZE);
                        check.iconSizeProperty().set(FONT_SIZE);
                        unchecked.iconSizeProperty().set(FONT_SIZE);

                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            btn.setContentDisplay(ContentDisplay.RIGHT);
                            buttonAndProgressBar.setAlignment(Pos.CENTER);
                            btn.setOnAction(event -> {
                                progressBar.styleProperty().set("");
                                GraphConfiguration graphConfiguration = getTableView().getItems().get(getIndex());
                                Task<FontIcon> connectionTask = new Task<>() {
                                    @Override
                                    protected FontIcon call() {
                                        updateProgress(ProgressBar.INDETERMINATE_PROGRESS, ProgressBar.INDETERMINATE_PROGRESS);
                                        updateValue(noneChecked);
                                        try {
                                            graphConfiguration.testGraphConnection();
                                            updateValue(check);
                                            progressBar.styleProperty().set("-fx-accent: green;");
                                            return check;
                                        } catch (Exception e) {
                                            progressBar.styleProperty().set("-fx-accent: red;");
                                            return unchecked;

                                        } finally {
                                            updateProgress(1D, 1D);
                                        }
                                    }
                                };
                                btn.graphicProperty().bind(connectionTask.valueProperty());
                                progressBar.progressProperty().bind(connectionTask.progressProperty());
                                Thread.ofVirtual().start(connectionTask);
                            });
                            setGraphic(buttonAndProgressBar);
                            setText(null);
                        }
                    }
                };
            }
        };
        testedCol.setCellFactory(cellFactory);

        this.graphTableView.getColumns().setAll(graphNameCol, urlCol, usernameCol, passwordCol, testedCol);
    }

}
