package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.core.LogEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

public class LogController {

    private final CodeArea logCodeArea;
    private final static int LOG_LENGTH = 5000;

    public LogController(VBox vBox) {
        this.logCodeArea = new CodeArea();
        VirtualizedScrollPane<CodeArea> resultVirtualizedScrollPane = new VirtualizedScrollPane<>(this.logCodeArea);
        vBox.getChildren().addAll(resultVirtualizedScrollPane);
        VBox.setVgrow(resultVirtualizedScrollPane, Priority.ALWAYS);
    }

    public void appendLog(LogEvent event) {
        Platform.runLater(() -> {
            int textLength = this.logCodeArea.getText().length();
            if (textLength > LOG_LENGTH) {
                String toSet = this.logCodeArea.getText(textLength - LOG_LENGTH, textLength);
                this.logCodeArea.clear();
                this.logCodeArea.appendText(toSet);
            }
            this.logCodeArea.appendText(event.getMessage().getFormattedMessage());
            this.logCodeArea.appendText("\n");
            this.logCodeArea.moveTo(this.logCodeArea.getText().length() - 1);
            this.logCodeArea.requestFollowCaret();
        });
    }
}
