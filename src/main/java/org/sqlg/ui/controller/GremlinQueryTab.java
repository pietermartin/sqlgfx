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
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinLexer;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinParser;
import org.apache.tinkerpop.gremlin.core.grammar.GremlinAntlrToJava;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.sqlg.ui.Fontawesome;
import org.sqlg.ui.model.GraphConfiguration;
import org.umlg.sqlg.structure.SqlgGraph;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.sqlg.ui.Fontawesome.Type.Solid;

public class GremlinQueryTab {

    private final SqlgGraph sqlgGraph;

    private final AtomicReference<Thread> atomicReference = new AtomicReference<>();
    private Button executeGremlin;
    private ProgressIndicator progressIndicator;
    private CodeArea gremlinCodeArea;
    private CodeArea resultCodeArea;

    public GremlinQueryTab(TabPane tabPane, GraphConfiguration graphConfiguration) {
        assert graphConfiguration.isOpen() : "graphConfiguration must be open";
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

    private Traversal parseGremlin(GraphTraversalSource g, final String script) {
        final GremlinLexer lexer = new GremlinLexer(CharStreams.fromString(script));
        final GremlinParser parser = new GremlinParser(new CommonTokenStream(lexer));
        final GremlinParser.QueryContext ctx = parser.query();
        return (Traversal) new GremlinAntlrToJava(g).visitQuery(ctx);
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
}
