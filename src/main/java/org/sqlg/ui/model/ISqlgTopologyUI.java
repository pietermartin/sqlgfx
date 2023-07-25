package org.sqlg.ui.model;

import javafx.beans.property.StringProperty;

public sealed interface ISqlgTopologyUI permits MetaTopology, GraphGroup, GraphConfiguration, SchemaUI, VertexLabelUI, EdgeLabelUI, EdgeRoleUI, PartitionUI, PropertyColumnUI, IndexUI {

    String getName();

    StringProperty nameProperty();

    default String getTopologyTypeName() {
        return "";
    }

    default void remove() {
        //noop
    };

}
