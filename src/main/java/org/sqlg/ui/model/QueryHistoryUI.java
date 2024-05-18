package org.sqlg.ui.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;

public class QueryHistoryUI {

    private final SimpleObjectProperty<GremlinHistory> queryHistory;
    private final SimpleStringProperty gremlin;
    private final SimpleObjectProperty<LocalDateTime> dateTime;
    private final SimpleStringProperty group;
    private final SimpleStringProperty graph;

    public QueryHistoryUI(GremlinHistory gremlinHistory) {
        this.queryHistory = new SimpleObjectProperty<>(gremlinHistory);
        this.gremlin = new SimpleStringProperty(gremlinHistory.getGremlin());
        this.dateTime = new SimpleObjectProperty<>(gremlinHistory.getExecutionDateTime());
        this.group = new SimpleStringProperty(gremlinHistory.getGroup());
        this.graph = new SimpleStringProperty(gremlinHistory.getGraph());
    }

    public GremlinHistory getQueryHistory() {
        return queryHistory.get();
    }

    public SimpleObjectProperty<GremlinHistory> queryHistoryProperty() {
        return queryHistory;
    }

    public String getGremlin() {
        return gremlin.get();
    }

    public SimpleStringProperty gremlinProperty() {
        return gremlin;
    }

    public LocalDateTime getDateTime() {
        return dateTime.get();
    }

    public SimpleObjectProperty<LocalDateTime> dateTimeProperty() {
        return dateTime;
    }

    public String getGroup() {
        return group.get();
    }

    public SimpleStringProperty groupProperty() {
        return group;
    }

    public String getGraph() {
        return graph.get();
    }

    public SimpleStringProperty graphProperty() {
        return graph;
    }
}
