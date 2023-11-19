package org.sqlg.ui.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import net.synedra.validatorfx.Validator;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;
import org.sqlg.ui.model.GraphConfiguration;
import org.sqlg.ui.model.GraphGroup;

public class GraphTableViewController extends BaseController {

    private final Stage stage;
    private final GraphGroup graphGroup;
    private final ObservableList<GraphConfiguration> graphConfigurations;
    private GraphConfiguration selectedGraphConfiguration;
    private final Validator validator = new Validator();
    protected final VBox root;

    public GraphTableViewController(Stage stage, GraphGroup graphGroup, ObservableList<GraphConfiguration> graphConfigurations) {
        super(stage);
        this.stage = stage;
        this.graphGroup = graphGroup;
        this.graphConfigurations = graphConfigurations;
        this.root = new VBox(10);
        this.root.setPadding(new Insets(10, 10, 10, 10));
        this.root.setMaxHeight(Double.MAX_VALUE);
        initialize();
    }

    protected void initialize() {
        TableView<GraphConfiguration> graphTableView = new TableView<>();
        graphTableView.setItems(this.graphConfigurations);
        graphTableView.setEditable(false);
        graphTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setPadding(new Insets(0, 5, 0, 0));
        Button addGraphButton = new Button("Add");
        ButtonBar.setButtonData(addGraphButton, ButtonBar.ButtonData.OK_DONE);
        Button deleteGraphButton = new Button("Delete");
        ButtonBar.setButtonData(deleteGraphButton, ButtonBar.ButtonData.CANCEL_CLOSE);
        buttonBar.getButtons().addAll(addGraphButton, deleteGraphButton);

        VBox vBox = new VBox(5, graphTableView, buttonBar);
        vBox.setPadding(new Insets(0, 0, 5, 0));
        VBox.setVgrow(buttonBar, Priority.NEVER);
        VBox.setVgrow(graphTableView, Priority.ALWAYS);
        VBox.setVgrow(vBox, Priority.ALWAYS);
        this.root.getChildren().add(vBox);

        deleteGraphButton.setDisable(true);

        TableColumn<GraphConfiguration, String> graphNameCol = new TableColumn<>(GraphConfiguration.NAME);
        graphNameCol.setPrefWidth(50D);
        graphNameCol.setCellValueFactory(new PropertyValueFactory<>(GraphConfiguration.NAME));
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

        TableColumn<GraphConfiguration, String> urlCol = new TableColumn<>(GraphConfiguration.URL);
        urlCol.setPrefWidth(150D);
        urlCol.setCellValueFactory(new PropertyValueFactory<>(GraphConfiguration.URL));
        TableColumn<GraphConfiguration, String> usernameCol = new TableColumn<>(GraphConfiguration.JDBC_USER);
        usernameCol.setCellValueFactory(new PropertyValueFactory<>(GraphConfiguration.JDBC_USER));
        usernameCol.setPrefWidth(50D);
        TableColumn<GraphConfiguration, GraphConfiguration.TESTED> testedCol = new TableColumn<>("");
        testedCol.setCellValueFactory(new PropertyValueFactory<>(GraphConfiguration._TESTED));

        graphTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.selectedGraphConfiguration = newValue;
                deleteGraphButton.setDisable(false);
            } else {
                deleteGraphButton.setDisable(true);
            }
        });

        addGraphButton.setOnAction(ignore -> new GraphConfigurationDialogController(stage, this.graphGroup));
        deleteGraphButton.setOnAction(ignore -> this.graphGroup.remove(this.selectedGraphConfiguration));

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

        graphTableView.getColumns().setAll(graphNameCol, urlCol, usernameCol, testedCol);
    }

    public Parent getView() {
        return this.root;
    }
}
