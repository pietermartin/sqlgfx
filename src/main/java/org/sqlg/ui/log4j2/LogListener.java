package org.sqlg.ui.log4j2;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.sqlg.ui.controller.LogController;

import java.io.Serializable;

public class LogListener {

    public static LogListener INSTANCE = new LogListener();
    private LogController logController = null;

    private LogListener() {
    }

    public void log(Layout<? extends Serializable> layout,  LogEvent event) {
        if (this.logController != null) {
            this.logController.appendLog(layout, event);
        }
    }

    public void setLogController(LogController logController) {
        this.logController = logController;
    }
}
