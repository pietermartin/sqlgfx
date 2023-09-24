package org.sqlg.ui.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.umlg.sqlg.structure.topology.Index;

public final class IndexUI implements ISqlgTopologyUI {

    private final VertexLabelUI vertexLabelUI;
    private final EdgeLabelUI edgeLabelUI;
    private final SimpleObjectProperty<Index> index;
    private SimpleStringProperty indexType;
    private SimpleStringProperty name;
    private SimpleBooleanProperty delete;

    public IndexUI(VertexLabelUI vertexLabelUI, EdgeLabelUI edgeLabelUI, Index index) {
        assert ((vertexLabelUI != null && edgeLabelUI == null) || (vertexLabelUI == null && edgeLabelUI != null)) : "vertexLabelUI or edgeLabelUI must be null";

        this.vertexLabelUI = vertexLabelUI;
        this.edgeLabelUI = edgeLabelUI;
        this.index = new SimpleObjectProperty<>(index);
        init(index);
    }

    private void init(Index index) {
        if (this.name == null) {
            this.name = new SimpleStringProperty(index.getName());
            this.indexType = new SimpleStringProperty(index.getIndexType().getName());
            this.delete = new SimpleBooleanProperty(false);
        } else {
            this.name.set(index.getName());
            this.indexType.set(index.getIndexType().getName());
            this.delete.set(false);
        }
    }

    public void reset() {
        init(this.index.get());
    }

    public Index getIndex() {
        return index.get();
    }

    public SimpleObjectProperty<Index> indexProperty() {
        return index;
    }

    public void setIndex(Index index) {
        this.index.set(index);
    }

    public String getIndexType() {
        return indexType.get();
    }

    public SimpleStringProperty indexTypeProperty() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType.set(indexType);
    }

    public VertexLabelUI getVertexLabelUI() {
        return vertexLabelUI;
    }

    public EdgeLabelUI getEdgeLabelUI() {
        return edgeLabelUI;
    }

    @Override
    public String getName() {
        return this.name.get();
    }

    @Override
    public StringProperty nameProperty() {
        return this.name;
    }

    public boolean isDelete() {
        return delete.get();
    }

    public SimpleBooleanProperty deleteProperty() {
        return delete;
    }

    @Override
    public String getTopologyTypeName() {
        return "Index";
    }

    @Override
    public void remove() {
        getIndex().remove();
    }

}
