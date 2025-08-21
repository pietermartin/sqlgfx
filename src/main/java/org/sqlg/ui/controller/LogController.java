package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.spi.StandardLevel;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.function.IntFunction;

public class LogController {

    private final CodeArea logCodeArea;
    private final static int LOG_LENGTH = 1000;
    private static final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.web("#ddd"), null, null));
    private static final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0);

    public LogController(VBox vBox) {
        this.logCodeArea = new CodeArea();
        this.logCodeArea.setParagraphGraphicFactory(new IntFunction<Node>() {
            @Override
            public Node apply(int value) {
                Label lineNo = new Label();
//                lineNo.setBackground(DEFAULT_BACKGROUND);
                lineNo.setAlignment(Pos.TOP_RIGHT);
                lineNo.setText("  ");
                return lineNo;
            }
        });
//        this.logCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(this.logCodeArea));
        VirtualizedScrollPane<StyleClassedTextArea> resultVirtualizedScrollPane = new VirtualizedScrollPane<>(this.logCodeArea);
        vBox.getChildren().addAll(resultVirtualizedScrollPane);
        VBox.setVgrow(resultVirtualizedScrollPane, Priority.ALWAYS);
    }

    public void appendLog(Layout<? extends Serializable> layout, LogEvent event) {
        int level = event.getLevel().intLevel();
        PatternLayout patternLayout = (PatternLayout) layout;
        StringBuilder sb = new StringBuilder();
        patternLayout.getEventSerializer().toSerializable(event, sb);
        String message = sb.toString();
        Platform.runLater(() -> {
            Val<Integer> val = LiveList.sizeOf(this.logCodeArea.getParagraphs());
            Integer numberOfRows = val.getOrElse(-1);
            if (numberOfRows > LOG_LENGTH) {
                int lastParagraphLength = this.logCodeArea.getParagraph(numberOfRows - LOG_LENGTH).length();
                this.logCodeArea.deleteText(0, 0, numberOfRows - LOG_LENGTH, lastParagraphLength);
            }

            int before = this.logCodeArea.getText().length();
            this.logCodeArea.appendText(message);
            int after = this.logCodeArea.getText().length();
            this.logCodeArea.moveTo(after - 1);
            this.logCodeArea.requestFollowCaret();

            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            spansBuilder.add(Collections.singleton(levelToStyle(level)), message.length());
            StyleSpans<Collection<String>> styleSpans = spansBuilder.create();
            this.logCodeArea.setStyleSpans(before, styleSpans);
        });
    }

    private String levelToStyle(int level) {
        StandardLevel standardLevel = StandardLevel.getStandardLevel(level);
        switch (standardLevel) {
            case OFF, ALL -> {
                return "";
            }
            case FATAL -> {
                return "log-fatal";
            }
            case ERROR -> {
                return "log-error";
            }
            case WARN -> {
                return "log-warn";
            }
            case INFO -> {
                return "log-info";
            }
            case DEBUG -> {
                return "log-debug";
            }
            case TRACE -> {
                return "log-trace";
            }
            default -> throw new IllegalStateException("Unhandled standardLevel " + standardLevel.name());
        }
    }

}
