package org.sqlg.ui.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

import java.util.ArrayList;
import java.util.List;

public class GremlinResultGrid {

    private final List<TableColumn<GremlinResultRow, Object>> headers = new ArrayList<>();
    private final ObservableList<GremlinResultRow> rows = FXCollections.observableArrayList();

    public List<TableColumn<GremlinResultRow, Object>> getHeaders() {
        return headers;
    }

    public ObservableList<GremlinResultRow> getRows() {
        return rows;
    }
}
