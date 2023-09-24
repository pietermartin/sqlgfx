package org.sqlg.ui.model;

import javafx.beans.property.*;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.PropertyColumn;

public final class PropertyColumnUI implements ISqlgTopologyUI {

    private final VertexLabelUI vertexLabelUI;
    private final EdgeLabelUI edgeLabelUI;
    private final SimpleObjectProperty<PropertyColumn> propertyColumn;
    //PropertyDefinition
    private SimpleStringProperty name;
    private SimpleStringProperty propertyType;
    //multiplicity
    private SimpleLongProperty lower;
    private SimpleLongProperty upper;
    private SimpleBooleanProperty unique;
    private SimpleBooleanProperty ordered;
    private SimpleStringProperty defaultLiteral;
    private SimpleStringProperty checkConstraint;
    private SimpleBooleanProperty delete;

    public PropertyColumnUI(VertexLabelUI vertexLabelUI, EdgeLabelUI edgeLabelUI, PropertyColumn propertyColumn) {
        assert ((vertexLabelUI != null && edgeLabelUI == null) || (vertexLabelUI == null && edgeLabelUI != null)) : "vertexLabelUI or edgeLabelUI must be null";

        this.vertexLabelUI = vertexLabelUI;
        this.edgeLabelUI = edgeLabelUI;
        this.propertyColumn = new SimpleObjectProperty<>(propertyColumn);
        init(propertyColumn);
    }

    private void init(PropertyColumn propertyColumn) {
        if (this.name == null) {
            this.name = new SimpleStringProperty(propertyColumn.getName());
            this.propertyType = new SimpleStringProperty(propertyColumn.getPropertyType().name());
            this.lower = new SimpleLongProperty(propertyColumn.getPropertyDefinition().multiplicity().lower());
            this.upper = new SimpleLongProperty(propertyColumn.getPropertyDefinition().multiplicity().upper());
            this.unique = new SimpleBooleanProperty(propertyColumn.getPropertyDefinition().multiplicity().unique());
            this.ordered = new SimpleBooleanProperty(propertyColumn.getPropertyDefinition().multiplicity().ordered());
            this.defaultLiteral = new SimpleStringProperty(propertyColumn.getPropertyDefinition().defaultLiteral());
            this.checkConstraint = new SimpleStringProperty(propertyColumn.getPropertyDefinition().checkConstraint());
            this.delete = new SimpleBooleanProperty(false);
        } else {
            this.name.set(propertyColumn.getName());
            this.propertyType.set(propertyColumn.getPropertyType().name());
            this.lower.set(propertyColumn.getPropertyDefinition().multiplicity().lower());
            this.upper.set(propertyColumn.getPropertyDefinition().multiplicity().upper());
            this.unique.set(propertyColumn.getPropertyDefinition().multiplicity().unique());
            this.ordered.set(propertyColumn.getPropertyDefinition().multiplicity().ordered());
            this.defaultLiteral.set(propertyColumn.getPropertyDefinition().defaultLiteral());
            this.checkConstraint.set(propertyColumn.getPropertyDefinition().checkConstraint());
            this.delete.set(false);
        }
    }

    public void reset() {
        PropertyColumn _propertyColumn = getVertexLabelUI().getVertexLabel().getProperty(this.propertyColumn.getValue().getName()).orElseThrow();
        init(_propertyColumn);
    }

    public PropertyColumn getPropertyColumn() {
        return propertyColumn.get();
    }

    public SimpleObjectProperty<PropertyColumn> propertyColumnProperty() {
        return propertyColumn;
    }

    public void setPropertyColumn(PropertyColumn propertyColumn) {
        this.name.set(propertyColumn.getName());
        this.propertyColumn.set(propertyColumn);
    }

    @Override
    public String getName() {
        return this.name.get();
    }

    @Override
    public StringProperty nameProperty() {
        return this.name;
    }

    public String getPropertyType() {
        return propertyType.get();
    }

    public SimpleStringProperty propertyTypeProperty() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType.set(propertyType);
    }

    public long getLower() {
        return lower.get();
    }

    public SimpleLongProperty lowerProperty() {
        return lower;
    }

    public void setLower(long lower) {
        this.lower.set(lower);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public long getUpper() {
        return upper.get();
    }

    public SimpleLongProperty upperProperty() {
        return upper;
    }

    public void setUpper(long upper) {
        this.upper.set(upper);
    }

    public boolean isUnique() {
        return unique.get();
    }

    public SimpleBooleanProperty uniqueProperty() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique.set(unique);
    }

    public boolean isOrdered() {
        return ordered.get();
    }

    public SimpleBooleanProperty orderedProperty() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered.set(ordered);
    }

    public String getDefaultLiteral() {
        return defaultLiteral.get();
    }

    public SimpleStringProperty defaultLiteralProperty() {
        return defaultLiteral;
    }

    public void setDefaultLiteral(String defaultLiteral) {
        this.defaultLiteral.set(defaultLiteral);
    }

    public String getCheckConstraint() {
        return checkConstraint.get();
    }

    public SimpleStringProperty checkConstraintProperty() {
        return checkConstraint;
    }

    public void setCheckConstraint(String checkConstraint) {
        this.checkConstraint.set(checkConstraint);
    }

    public boolean isDelete() {
        return delete.get();
    }

    public SimpleBooleanProperty deleteProperty() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete.set(delete);
    }

    public VertexLabelUI getVertexLabelUI() {
        return vertexLabelUI;
    }

    public EdgeLabelUI getEdgeLabelUI() {
        return edgeLabelUI;
    }

    @Override
    public String getTopologyTypeName() {
        return "PropertyColumn";
    }

    @Override
    public void remove() {
        getPropertyColumn().remove();
    }

    public void selectInTree(String name) {
        GraphConfiguration graphConfiguration;
        if (getVertexLabelUI() != null) {
            graphConfiguration = this.getVertexLabelUI().getSchemaUI().getGraphConfiguration();
            GraphGroup graphGroup = graphConfiguration.getGraphGroup();
            graphConfiguration
                    .getLeftPaneController()
                    .selectPropertyColumn(
                            graphGroup,
                            graphConfiguration,
                            getVertexLabelUI().getSchemaUI().getSchema(),
                            getVertexLabelUI().getVertexLabel(),
                            name
                    );
        } else {
            graphConfiguration = this.getEdgeLabelUI().getSchemaUI().getGraphConfiguration();
            GraphGroup graphGroup = graphConfiguration.getGraphGroup();
            graphConfiguration
                    .getLeftPaneController()
                    .selectPropertyColumn(
                            graphGroup,
                            graphConfiguration,
                            getEdgeLabelUI().getSchemaUI().getSchema(),
                            getEdgeLabelUI().getEdgeLabel(),
                            name
                    );
        }
    }

    private SqlgGraph sqlgGraph() {
        if (this.vertexLabelUI != null) {
            return this.vertexLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
        } else {
            return this.edgeLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
        }
    }

}
