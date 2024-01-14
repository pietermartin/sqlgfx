package org.sqlg.ui.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.umlg.sqlg.structure.topology.Index;
import org.umlg.sqlg.structure.topology.PropertyColumn;

public final class IndexUI implements ISqlgTopologyUI {

    private final VertexLabelUI vertexLabelUI;
    private final EdgeLabelUI edgeLabelUI;
    private final SimpleObjectProperty<Index> index;
    private SimpleStringProperty indexType;
    private SimpleStringProperty name;
    private SimpleBooleanProperty delete;
    private ObservableList<PropertyColumnUI> propertyColumnUIs;

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
            this.propertyColumnUIs = FXCollections.observableArrayList();
            for (PropertyColumn propertyColumn : index.getProperties()) {
                if (vertexLabelUI == null) {
                    this.propertyColumnUIs.add(new PropertyColumnUI(null, this.edgeLabelUI, propertyColumn));
                } else {
                    this.propertyColumnUIs.add(new PropertyColumnUI(this.vertexLabelUI, null, propertyColumn));
                }
            }
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
    public String getQualifiedName() {
        if (getVertexLabelUI() != null) {
            return STR."\{getVertexLabelUI().getQualifiedName()}.\{getIndex().getName()}";
        } else {
            return STR."\{getEdgeLabelUI().getQualifiedName()}.\{getIndex().getName()}";
        }
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

    public ObservableList<PropertyColumnUI> getPropertyColumnUIs() {
        return propertyColumnUIs;
    }

    public void setPropertyColumnUIs(ObservableList<PropertyColumnUI> propertyColumnUIs) {
        this.propertyColumnUIs = propertyColumnUIs;
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
