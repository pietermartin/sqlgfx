package org.sqlg.ui.controller;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.*;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.EdgeLabel;
import org.umlg.sqlg.structure.topology.EdgeRole;
import org.umlg.sqlg.structure.topology.Schema;
import org.umlg.sqlg.structure.topology.VertexLabel;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EdgeLabelFormController extends AbstractLabelControllerName {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeLabelFormController.class);
    private final EdgeLabelUI edgeLabelUI;

    public EdgeLabelFormController(LeftPaneController leftPaneController, EdgeLabelUI edgeLabelUI) {
        super(leftPaneController, edgeLabelUI);
        this.edgeLabelUI = edgeLabelUI;
    }

    @Override
    protected SqlgGraph getSqlgGraph() {
        return this.edgeLabelUI.getSchemaUI().getGraphConfiguration().getSqlgGraph();
    }

    @Override
    protected void delete() {
        SchemaUI schemaUI = this.edgeLabelUI.getSchemaUI();
        GraphConfiguration graphConfiguration = schemaUI.getGraphConfiguration();
        GraphGroup graphGroup = graphConfiguration.getGraphGroup();
        EdgeLabel edgeLabel = this.edgeLabelUI.getEdgeLabel();
        edgeLabel.remove();
        for (EdgeRole outEdgeRole : edgeLabel.getOutEdgeRoles()) {
            this.leftPaneController.deleteEdgeRole(
                    graphGroup,
                    graphConfiguration,
                    outEdgeRole,
                    Direction.OUT
            );
        }
        for (EdgeRole inEdgeRole : edgeLabel.getInEdgeRoles()) {
            this.leftPaneController.deleteEdgeRole(
                    graphGroup,
                    graphConfiguration,
                    inEdgeRole,
                    Direction.IN
            );
        }
        this.leftPaneController.deleteEdgeLabel(
                graphGroup,
                graphConfiguration,
                schemaUI.getSchema(),
                edgeLabel
        );
    }

    @Override
    protected void rename() {
        if (!this.edgeLabelUI.getEdgeLabel().getName().equals(this.sqlgTreeDataFormNameTxt.getText())) {
            SqlgGraph sqlgGraph = getSqlgGraph();
            try {
                SchemaUI schemaUI = this.edgeLabelUI.getSchemaUI();
                EdgeLabel edgeLabel = this.edgeLabelUI.getEdgeLabel();
                Schema schema = edgeLabel.getSchema();

                edgeLabel.rename(this.sqlgTreeDataFormNameTxt.getText());
                sqlgGraph.tx().commit();
                EdgeLabel renamedEdgeLabel = schema.getEdgeLabel(this.sqlgTreeDataFormNameTxt.getText()).orElseThrow();
                this.edgeLabelUI.setEdgeLabel(renamedEdgeLabel);

                for (EdgeRole outEdgeRole : renamedEdgeLabel.getOutEdgeRoles()) {
                    VertexLabel vertexLabel = outEdgeRole.getVertexLabel();
                    Set<EdgeRoleUI> edgeRoleUIs = schemaUI.getVertexLabelUIs().stream()
                            .filter(v -> v.getVertexLabel().equals(vertexLabel))
                            .flatMap(v -> v.getOutEdgeRoleUIs().stream())
                            .filter(er -> er.getEdgeRole().getEdgeLabel().getName().equals(edgeLabel.getName()))
                            .collect(Collectors.toSet());
                    for (EdgeRoleUI edgeRoleUI : edgeRoleUIs) {
                        edgeRoleUI.setEdgeRole(outEdgeRole);
                    }
                }
                //in edge roles could be in a different schema so need to go through them all
                for (EdgeRole inEdgeRole : renamedEdgeLabel.getInEdgeRoles()) {
                    VertexLabel vertexLabel = inEdgeRole.getVertexLabel();
                    for (SchemaUI otherSchemaUi : schemaUI.getGraphConfiguration().getSchemaUis()) {
                        Set<EdgeRoleUI> edgeRoleUIs = otherSchemaUi.getVertexLabelUIs().stream()
                                .filter(v -> v.getVertexLabel().equals(vertexLabel))
                                .flatMap(v -> v.getInEdgeRoleUIs().stream())
                                .filter(er -> er.getEdgeRole().getEdgeLabel().getName().equals(edgeLabel.getName()))
                                .collect(Collectors.toSet());
                        for (EdgeRoleUI edgeRoleUI : edgeRoleUIs) {
                            edgeRoleUI.setEdgeRole(inEdgeRole);
                        }
                    }
                }

                this.leftPaneController.refreshTree();
                showDialog(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Renamed EdgeLabel to '" + this.sqlgTreeDataFormNameTxt.getText() + "'"
                );
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                showDialog(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to renamed EdgeLabel to '" + this.sqlgTreeDataFormNameTxt.getText() + "'",
                        e,
                        result -> this.sqlgTreeDataFormNameTxt.setText(this.edgeLabelUI.getEdgeLabel().getName())
                );
            } finally {
                sqlgGraph.tx().rollback();
            }
        }

    }

    @Override
    protected Collection<Node> additionalChildren(ISqlgTopologyUI sqlgTopologyUI) {
        EdgeLabelUI edgeLabelUI = (EdgeLabelUI) sqlgTopologyUI;
        Node propertiesTableView = ControllerUtil.propertyColumnsTableView(
                edgeLabelUI.getPropertyColumnUIs(),
                this.editToggleSwitch.selectedProperty(),
                event -> ControllerUtil.savePropertyColumns(
                        getSqlgGraph(),
                        edgeLabelUI.getEdgeLabel(),
                        edgeLabelUI.getPropertyColumnUIs(),
                        result -> {
                            assert result : "expected true";
                            showDialog(
                                    Alert.AlertType.INFORMATION,
                                    "Success",
                                    "Saved PropertyColumns"
                            );
                            return null;
                        },
                        exception -> {
                            showDialog(
                                    Alert.AlertType.ERROR,
                                    "Error",
                                    "Failed to save PropertyColumns",
                                    exception,
                                    result -> this.sqlgTreeDataFormNameTxt.setText(edgeLabelUI.getEdgeLabel().getName())
                            );
                            return null;
                        }
                ),
                event -> cancelPropertyColumns()
        );
        VBox.setVgrow(propertiesTableView, Priority.ALWAYS);
        return List.of(propertiesTableView);
    }


}
