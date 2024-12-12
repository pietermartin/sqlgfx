package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Callback;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinAntlrToJava;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinLexer;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinParser;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.controlsfx.control.ToggleSwitch;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.Fontawesome;
import org.sqlg.ui.model.*;
import org.umlg.sqlg.structure.*;
import org.umlg.sqlg.structure.topology.AbstractLabel;
import org.umlg.sqlg.structure.topology.Schema;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.sqlg.ui.Fontawesome.Type.Solid;

@SuppressWarnings("RegExpDuplicateAlternationBranch")
public class GremlinQueryTab {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    private final Logger LOGGER = LoggerFactory.getLogger(GremlinQueryTab.class);
    private final SqlgGraph sqlgGraph;
    private final GraphConfiguration graphConfiguration;

    private final AtomicReference<Thread> atomicReference = new AtomicReference<>();
    private Button executeGremlin;
    private ToggleSwitch topologyToggleSwitch;
    private ProgressIndicator progressIndicator;
    private CodeArea gremlinCodeArea;


    private final TabPane resultTabPane = new TabPane();
    private CodeArea resultCodeArea;
    private TableView<GremlinResultRow> resultAsGridTableView;

    private static final String[] KEYWORDS_1 = new String[]{
            "V", "out", "in", "select",
            "toList", "toSet", "hasLabel", "has",
            "optional"
    };

    private static final String[] KEYWORDS_2 = new String[]{
            "as"
    };

    private static final String[] KEYWORDS_3 = new String[]{
            "g"
    };

    private static final String KEYWORD_1_PATTERN = STR."\\b(\{String.join("|", KEYWORDS_1)})\\b";
    private static final String KEYWORD_2_PATTERN = STR."\\b(\{String.join("|", KEYWORDS_2)})\\b";
    private static final String KEYWORD_3_PATTERN = STR."\\b(\{String.join("|", KEYWORDS_3)})\\b";
    private static final String PAREN_PATTERN = "[()]";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String BRACKET_PATTERN = "[\\[\\]]";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"   // for whole text processing (text blocks)
            + "|" + "/\\*[^\\v]*" + "|" + "^\\h*\\*([^\\v]*|/)";  // for visible paragraph processing (line by line)

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD1>" + KEYWORD_1_PATTERN + ")"
                    + "|(?<KEYWORD2>" + KEYWORD_2_PATTERN + ")"
                    + "|(?<KEYWORD3>" + KEYWORD_3_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public GremlinQueryTab(TabPane tabPane, GraphConfiguration graphConfiguration) {
        assert graphConfiguration.isOpen() : "graphConfiguration must be open";
        this.graphConfiguration = graphConfiguration;
        this.sqlgGraph = graphConfiguration.getSqlgGraph();
        Node node = prepareGremlinTab();
        Tab tab = new Tab(graphConfiguration.getName(), node);
        tab.setGraphic(Fontawesome.MAGNIFYING_GLASS.label(Solid));
        tabPane.getTabs().add(tab);
    }

    private Node prepareGremlinTab() {
        BorderPane borderPane = new BorderPane();

        ToolBar toolBar = new ToolBar();
        borderPane.setTop(toolBar);
        this.executeGremlin = new Button();
        this.executeGremlin.setGraphic(Fontawesome.PLAY.label(Solid, 15));
        Button cancelGremlin = new Button();
        cancelGremlin.setGraphic(Fontawesome.STOP.label(Solid, 15));

        this.topologyToggleSwitch = new ToggleSwitch("Topology");
        this.topologyToggleSwitch.setLayoutX(70);
        this.topologyToggleSwitch.setLayoutY(168);

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button queryHistory = new Button();
        queryHistory.setGraphic(Fontawesome.RECTANGLE_HISTORY.label(Solid, 15));
        toolBar.getItems().addAll(executeGremlin, cancelGremlin, space, topologyToggleSwitch, queryHistory);

        StackPane stackPane = new StackPane();
        borderPane.setCenter(stackPane);

        VBox vBox = new VBox();
        BorderPane queryHistoryPane = new BorderPane();
        queryHistoryPane.setVisible(false);
        queryHistory.setOnAction(ignore -> queryHistoryPane.setVisible(!queryHistoryPane.isVisible()));
        queryHistoryPane.setMaxWidth(600);
        queryHistoryPane.setStyle("-fx-background-color:#55555550");
        stackPane.getChildren().addAll(vBox, queryHistoryPane);
        StackPane.setAlignment(queryHistoryPane, Pos.CENTER_RIGHT);

        TableView<QueryHistoryUI> queryHistoryUITableView = new TableView<>();
        queryHistoryUITableView.setEditable(false);
        queryHistoryUITableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);
        queryHistoryUITableView.setRowFactory(ignore -> {
            TableRow<QueryHistoryUI> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    QueryHistoryUI rowData = row.getItem();
                    gremlinCodeArea.appendText("\n");
                    gremlinCodeArea.appendText(rowData.getGremlin());
                }
            });
            return row;
        });

        TableColumn<QueryHistoryUI, String> queryColumn = new TableColumn<>("gremlin");
        queryColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        queryColumn.setCellValueFactory(new PropertyValueFactory<>("gremlin"));

        TableColumn<QueryHistoryUI, LocalDateTime> dateTimeColumn = new TableColumn<>("dateTime");
        dateTimeColumn.setCellFactory(ignore -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(formatter.format(dateTime));
                }
            }
        });
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        dateTimeColumn.setMaxWidth(150);

        TableColumn<QueryHistoryUI, String> groupColumn = new TableColumn<>("group");
        groupColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        groupColumn.setCellValueFactory(new PropertyValueFactory<>("group"));
        groupColumn.setMaxWidth(70);

        TableColumn<QueryHistoryUI, String> graphColumn = new TableColumn<>("graph");
        graphColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        graphColumn.setCellValueFactory(new PropertyValueFactory<>("graph"));
        graphColumn.setMaxWidth(70);

        queryHistoryUITableView.getColumns().addAll(queryColumn, dateTimeColumn, groupColumn, graphColumn);
        queryHistoryUITableView.setItems(this.graphConfiguration.getGraphGroup().getUser().getQueryHistoryUIS());

        VBox queryHistoryVBox = new VBox(5, queryHistoryUITableView);
        queryHistoryVBox.setPadding(new Insets(0, 0, 5, 0));
        VBox.setVgrow(queryHistoryVBox, Priority.ALWAYS);
        VBox.setVgrow(queryHistoryUITableView, Priority.ALWAYS);
        queryHistoryPane.setCenter(queryHistoryVBox);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPosition(0, 0.25D);

        this.gremlinCodeArea = new CodeArea();
        // recompute the syntax highlighting for all text, 500 ms after user stops editing area
        // Note that this shows how it can be done but is not recommended for production with
        // large files as it does a full scan of ALL the text every time there is a change !
        @SuppressWarnings("unused")
        Subscription cleanupWhenNoLongerNeedIt = this.gremlinCodeArea
                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                //   when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()
                // do not emit an event until 500 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(500))
                // run the following code block when previous stream emits an event
                .subscribe(ignore -> this.gremlinCodeArea.setStyleSpans(0, computeHighlighting(this.gremlinCodeArea.getText())));

        // when no longer need syntax highlighting and wish to clean up memory leaks
        // run: `cleanupWhenNoLongerNeedIt.unsubscribe();`

        final KeyCombination kb = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
        this.gremlinCodeArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (kb.match(event)) {
                executeGremlin();
            }
        });
        // add line numbers to the left of area
        this.gremlinCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(this.gremlinCodeArea));
        VirtualizedScrollPane<CodeArea> gremlinVirtualizedScrollPane = new VirtualizedScrollPane<>(this.gremlinCodeArea);

        VBox buttonAndCodeArea = new VBox();
        buttonAndCodeArea.getChildren().addAll(gremlinVirtualizedScrollPane);
        VBox.setVgrow(gremlinVirtualizedScrollPane, Priority.ALWAYS);


        this.resultCodeArea = new CodeArea();
        this.resultCodeArea.setEditable(false);

        // add line numbers to the left of area
        this.resultCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(this.resultCodeArea));
        VirtualizedScrollPane<CodeArea> resultVirtualizedScrollPane = new VirtualizedScrollPane<>(this.resultCodeArea);

        Tab resultCodeAreaTab = new Tab("Result as string", null);
        Tab resultAsGridTab = new Tab("Result in a grid", null);
        this.resultTabPane.getTabs().addAll(resultAsGridTab, resultCodeAreaTab);
        resultCodeAreaTab.setContent(resultVirtualizedScrollPane);

        this.resultAsGridTableView = new TableView<>();
        this.resultAsGridTableView.setEditable(false);
        this.resultAsGridTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);

        VBox gremlinResultTableViewVBox = new VBox(5, this.resultAsGridTableView);
        gremlinResultTableViewVBox.setPadding(new Insets(0, 0, 0, 0));
        VBox.setVgrow(gremlinResultTableViewVBox, Priority.ALWAYS);
        VBox.setVgrow(this.resultAsGridTableView, Priority.ALWAYS);
        resultAsGridTab.setContent(gremlinResultTableViewVBox);

        splitPane.getItems().addAll(buttonAndCodeArea, this.resultTabPane);
        vBox.getChildren().addAll(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        this.progressIndicator = new ProgressIndicator();
        this.progressIndicator.setMaxHeight(14);
        this.progressIndicator.setMaxWidth(14);

        this.executeGremlin.setOnAction(ignore -> executeGremlin());
        cancelGremlin.setOnAction(ignore -> {
            Thread thread = this.atomicReference.get();
            if (thread != null) {
                thread.interrupt();
            }
        });
        return borderPane;
    }

    private Traversal<?, ?> parseGremlin(GraphTraversalSource g, final String script) {
        final GremlinLexer lexer = new GremlinLexer(CharStreams.fromString(script));
        final GremlinParser parser = new GremlinParser(new CommonTokenStream(lexer));
        final GremlinParser.QueryContext ctx = parser.query();
        return (Traversal<?, ?>) new GremlinAntlrToJava(g).visitQuery(ctx);
    }

    private void executeGremlin() {
        Platform.runLater(resultCodeArea::clear);
        executeGremlin.setGraphic(progressIndicator);
        atomicReference.set(Thread.startVirtualThread(() -> {
            try {
                StopWatch stopWatch = StopWatch.createStarted();
                LOGGER.info("=== gremlin start ===");

                String selectedText = gremlinCodeArea.getText(gremlinCodeArea.getSelection());
                String gremlin;
                if (!StringUtils.isEmpty(selectedText.trim())) {
                    gremlin = selectedText.trim();
                } else {
                    gremlin = gremlinCodeArea.textProperty().getValue().trim();
                }
                LOGGER.debug(gremlin);
                Traversal<?, ?> traversal = parseGremlin(
                        this.topologyToggleSwitch.isSelected() ? sqlgGraph.topology() : sqlgGraph.traversal(),
                        gremlin
                );
                Pair<String, GremlinResultGrid> result = traversalResult(traversal);
                String resultAsString = result.getLeft();
                GremlinResultGrid gremlinResultGrid = result.getRight();
                Platform.runLater(() -> {
                    this.resultAsGridTableView.getColumns().clear();
                    this.resultAsGridTableView.getColumns().addAll(gremlinResultGrid.getHeaders());
                    this.resultAsGridTableView.setItems(gremlinResultGrid.getRows());
                });

                Thread.ofVirtual().start(() -> {
                    User user = this.graphConfiguration.getGraphGroup().getUser();
                    user.getQueryHistoryUIS().addFirst(
                            new QueryHistoryUI(
                                    new GremlinHistory(gremlin, LocalDateTime.now(), this.graphConfiguration.getGraphGroup().getName(), this.graphConfiguration.getName())
                            )
                    );
                    user.getRoot().persistConfig();
                });
                stopWatch.stop();
                LOGGER.info("=== gremlin end ===");
                LOGGER.info("execution time: {}", stopWatch);
                Platform.runLater(() -> {
                    resultCodeArea.appendText(resultAsString);
                    executeGremlin.setGraphic(Fontawesome.PLAY.label(Solid, 15));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    executeGremlin.setGraphic(Fontawesome.PLAY.label(Solid, 15));
                    resultCodeArea.clear();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    resultCodeArea.appendText(sw.toString());
                });
            } finally {
                sqlgGraph.tx().rollback();
            }
        }));
    }

    private Pair<String, GremlinResultGrid> traversalResult(Object traversal) {
        GremlinResultGrid gremlinResultGrid = new GremlinResultGrid();

        TableColumn<GremlinResultRow, Object> idColumn = new TableColumn<>("id");
        idColumn.setMaxWidth(250);
        idColumn.setCellFactory(new Callback<TableColumn<GremlinResultRow, Object>, TableCell<GremlinResultRow, Object>>() {
            @Override
            public TableCell<GremlinResultRow, Object> call(TableColumn<GremlinResultRow, Object> param) {
                return new PropertyDefinitionCell();
            }
        });
        idColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GremlinResultRow, Object>, ObservableValue<Object>>() {
            public ObservableValue<Object> call(TableColumn.CellDataFeatures<GremlinResultRow, Object> p) {
                return p.getValue().idProperty();
            }
        });

        gremlinResultGrid.getHeaders().add(idColumn);

        Iterator<?> tempIterator = Collections.emptyIterator();
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        label:
        while (true) {
            if (tempIterator.hasNext()) {
                while (tempIterator.hasNext()) {
                    final Object object = tempIterator.next();
                    String label;
                    String id;
                    GremlinResultRow gremlinResultRow = new GremlinResultRow();
                    if (object instanceof SqlgElement sqlgElement) {
                        label = sqlgElement.label();
                        SchemaTable schemaTable = sqlgElement.getSchemaTable();
                        Schema schema = sqlgGraph.getTopology().getSchema(schemaTable.getSchema()).orElseThrow();
                        AbstractLabel abstractLabel;
                        if (sqlgElement instanceof SqlgEdge sqlgEdge) {
                            abstractLabel = schema.getEdgeLabel(schemaTable.getTable()).orElseThrow();
                        } else if (sqlgElement instanceof SqlgVertex sqlgVertex) {
                            abstractLabel = schema.getVertexLabel(schemaTable.getTable()).orElseThrow();
                        } else {
                            throw new IllegalStateException("Unknown sqlgElement " + sqlgElement.getClass().getSimpleName());
                        }
                        id = sqlgElement.id().toString();
                        gremlinResultRow.setLabel(label);
                        gremlinResultRow.setId(id);
                        Iterator<? extends Property<?>> iterator = sqlgElement.properties();
                        while (iterator.hasNext()) {
                            Property<?> p = iterator.next();
                            String key = p.key();
                            Object value = p.value();
                            gremlinResultRow.add(key, value);
                            if (first) {
                                TableColumn<GremlinResultRow, Object> column = new TableColumn<>(p.key());
                                column.setCellFactory(new Callback<TableColumn<GremlinResultRow, Object>, TableCell<GremlinResultRow, Object>>() {
                                    @Override
                                    public TableCell<GremlinResultRow, Object> call(TableColumn<GremlinResultRow, Object> param) {
                                        return new PropertyDefinitionCell();
                                    }
                                });
                                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GremlinResultRow, Object>, ObservableValue<Object>>() {
                                    public ObservableValue<Object> call(TableColumn.CellDataFeatures<GremlinResultRow, Object> p) {
                                        return p.getValue().get(key);
                                    }
                                });
                                gremlinResultGrid.getHeaders().add(column);
                            }
                        }
                        first = false;
                        gremlinResultGrid.getRows().add(gremlinResultRow);
                    }
                    sb.append(((null == object) ? null : object.toString()));
                    if (tempIterator.hasNext()) {
                        sb.append("\n");
                    }
                }
                break;
            } else {
                switch (traversal) {
                    case Iterator<?> iterator:
                        tempIterator = iterator;
                        if (!tempIterator.hasNext()) break label;
                        break;
                    case Iterable<?> iterable:
                        tempIterator = iterable.iterator();
                        if (!tempIterator.hasNext()) break label;
                        break;
                    case Object[] objects:
                        tempIterator = new ArrayIterator<>(traversal);
                        if (!tempIterator.hasNext()) break label;
                        break;
                    case Map<?, ?> map:
                        tempIterator = map.entrySet().iterator();
                        if (!tempIterator.hasNext()) break label;
                        break;
                    case null:
                    default:
                        sb.append(((null == traversal) ? null : traversal.toString()));
                        break label;
                }
            }
        }
        return Pair.of(sb.toString(), gremlinResultGrid);
    }

    private <V> Callback<TableColumn.CellDataFeatures<GremlinResultRow, V>, ObservableValue<V>> cellFactoryForPropertyColumn(PropertyType propertyType, String key, V value) {
//        switch (propertyType.ordinal()) {
//            case PropertyType.STRING_ORDINAL:
//                Callback<TableColumn.CellDataFeatures<GremlinResultRow, String>, ObservableValue<String>> callback = new Callback<TableColumn.CellDataFeatures<GremlinResultRow, String>, ObservableValue<String>>() {
//                    public ObservableValue<String> call(TableColumn.CellDataFeatures<GremlinResultRow, String> p) {
//                        return new ReadOnlyObjectWrapper<>((String)value);
//                    }
//                };
//                return callback;
//            case PropertyType.SHORT_ORDINAL:
//            case PropertyType.INTEGER_ORDINAL:
//            case PropertyType.LONG_ORDINAL:
//            case PropertyType.FLOAT_ORDINAL:
//            case PropertyType.DOUBLE_ORDINAL:
//            default:
//        }
        Callback<TableColumn.CellDataFeatures<GremlinResultRow, V>, ObservableValue<V>> callback = new Callback<TableColumn.CellDataFeatures<GremlinResultRow, V>, ObservableValue<V>>() {
            public ObservableValue<V> call(TableColumn.CellDataFeatures<GremlinResultRow, V> p) {
                return new ReadOnlyObjectWrapper<>(value);
            }
        };
        return callback;
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD1") != null ? "keyword1" :
                            matcher.group("KEYWORD2") != null ? "keyword2" :
                                    matcher.group("KEYWORD3") != null ? "keyword3" :
                                            matcher.group("PAREN") != null ? "paren" :
                                                    matcher.group("BRACE") != null ? "brace" :
                                                            matcher.group("BRACKET") != null ? "bracket" :
                                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                                            matcher.group("STRING") != null ? "string" :
                                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                                            null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

}
