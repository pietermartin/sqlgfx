package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinAntlrToJava;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinLexer;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinParser;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;
import org.sqlg.ui.Fontawesome;
import org.sqlg.ui.model.GraphConfiguration;
import org.umlg.sqlg.structure.SqlgGraph;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.sqlg.ui.Fontawesome.Type.Solid;

public class GremlinQueryTab {

    private final SqlgGraph sqlgGraph;

    private final Stage stage;
    private final AtomicReference<Thread> atomicReference = new AtomicReference<>();
    private Button executeGremlin;
    private ProgressIndicator progressIndicator;
    private CodeArea gremlinCodeArea;
    private CodeArea resultCodeArea;

    private static final String[] SPECIAL = new String[] {
            "g"
    };

    private static final String[] KEYWORDS = new String[] {
            "V", "out", "in", "select",
            "toList", "toSet", "hasLabel", "has"
    };

    private static final String SPECIAL_PATTERN = "\\b(" + String.join("|", SPECIAL) + ")\\b";
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"   // for whole text processing (text blocks)
            + "|" + "/\\*[^\\v]*" + "|" + "^\\h*\\*([^\\v]*|/)";  // for visible paragraph processing (line by line)

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<SPECIAL>" + SPECIAL_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public GremlinQueryTab(Stage stage, TabPane tabPane, GraphConfiguration graphConfiguration) {
        assert graphConfiguration.isOpen() : "graphConfiguration must be open";
        this.stage = stage;
        this.sqlgGraph = graphConfiguration.getSqlgGraph();
        Node node = prepareGremlinTab();
        Tab tab = new Tab(graphConfiguration.getName(), node);
        tab.setGraphic(Fontawesome.MAGNIFYING_GLASS.label(Solid));
        tabPane.getTabs().add(tab);
    }

    private Node prepareGremlinTab() {
        HBox buttonHBox = new HBox(5);
        buttonHBox.setAlignment(Pos.CENTER_LEFT);
        buttonHBox.setPadding(new Insets(5, 5, 5, 5));
        this.executeGremlin = new Button();
        this.executeGremlin.setGraphic(Fontawesome.PLAY.label(Solid, 15));
        Button cancelGremlin = new Button();
        cancelGremlin.setGraphic(Fontawesome.STOP.label(Solid, 15));
        buttonHBox.getChildren().addAll(executeGremlin, cancelGremlin);

        VBox vBox = new VBox();
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPosition(0, 0.25D);

        this.gremlinCodeArea = new CodeArea();
        // recompute the syntax highlighting for all text, 500 ms after user stops editing area
        // Note that this shows how it can be done but is not recommended for production with
        // large files as it does a full scan of ALL the text every time there is a change !
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
        buttonAndCodeArea.getChildren().addAll(buttonHBox, gremlinVirtualizedScrollPane);
        VBox.setVgrow(gremlinVirtualizedScrollPane, Priority.ALWAYS);

        this.resultCodeArea = new CodeArea();
        this.resultCodeArea.setEditable(false);

        // add line numbers to the left of area
        this.resultCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(this.resultCodeArea));
        VirtualizedScrollPane<CodeArea> resultVirtualizedScrollPane = new VirtualizedScrollPane<>(this.resultCodeArea);

        splitPane.getItems().addAll(buttonAndCodeArea, resultVirtualizedScrollPane);
        vBox.getChildren().addAll(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        this.progressIndicator = new ProgressIndicator();
        this.progressIndicator.setMaxHeight(14);
        this.progressIndicator.setMaxWidth(14);

        this.executeGremlin.setOnAction(ignore -> {
            executeGremlin();
        });
        cancelGremlin.setOnAction(ignore -> {
            Thread thread = this.atomicReference.get();
            if (thread != null) {
                thread.interrupt();
            }
        });
        return vBox;
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
                Traversal<?, ?> traversal = parseGremlin(
                        sqlgGraph.traversal(),
                        gremlinCodeArea.textProperty().getValue()
                );
                String result = traversalResultToString(traversal);
                Platform.runLater(() -> {
                    resultCodeArea.appendText(result);
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

    public String traversalResultToString(Object traversal) {
        Iterator<?> tempIterator = Collections.emptyIterator();
        StringBuilder sb = new StringBuilder();
        label:
        while (true) {
            if (tempIterator.hasNext()) {
                while (tempIterator.hasNext()) {
                    final Object object = tempIterator.next();
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
        return sb.toString();
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

}
