package org.sqlg.ui.model;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.controller.LeftPaneController;
import org.umlg.sqlg.structure.SqlgDataSource;
import org.umlg.sqlg.structure.SqlgDataSourceFactory;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public final class GraphConfiguration implements ISqlgTopologyUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphConfiguration.class);
    private final LeftPaneController leftPaneController;
    private SqlgGraph sqlgGraph;
    private BooleanProperty sqlgGraphOpenProperty = new SimpleBooleanProperty(false);
    private final GraphGroup graphGroup;
    private final ObservableList<SchemaUI> schemaUis = FXCollections.observableArrayList(new ArrayList<>());

    public enum TESTED {
        UNTESTED,
        FALSE,
        TRUE;
    }

    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty url = new SimpleStringProperty(this, "url");
    private final StringProperty username = new SimpleStringProperty(this, "username");
    private final StringProperty password = new SimpleStringProperty(this, "password");
    private final ObjectProperty<TESTED> tested = new SimpleObjectProperty<>(TESTED.UNTESTED, "tested");

    public GraphConfiguration(
            LeftPaneController leftPaneController,
            GraphGroup graphGroup,
            String name,
            String url,
            String username,
            String password,
            TESTED tested) {

        this.leftPaneController = leftPaneController;
        this.graphGroup = graphGroup;
        this.name.set(name);
        this.url.set(url);
        this.username.set(username);
        this.password.set(password);
        this.tested.set(tested);

    }

    public GraphGroup getGraphGroup() {
        return graphGroup;
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public TESTED getTested() {
        return tested.get();
    }

    public ObjectProperty<TESTED> testedProperty() {
        return tested;
    }

    public void setTested(TESTED tested) {
        this.tested.set(tested);
    }

    public ObservableList<SchemaUI> getSchemaUis() {
        return this.schemaUis;
    }

    public void testGraphConnection() {
        SqlgDataSource dataSource = null;
        try {
            Configuration configuration = new MapConfiguration(new HashMap<>() {{
                put("jdbc.url", getUrl());
                put("jdbc.username", getUsername());
                put("jdbc.password", getPassword());
            }});
            dataSource = SqlgDataSourceFactory.create(configuration);
            dataSource.getDatasource().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

    public void openSqlgGraph() {
        Configuration configuration = new MapConfiguration(new HashMap<>() {{
            put("jdbc.url", getUrl());
            put("jdbc.username", getUsername());
            put("jdbc.password", getPassword());
            put("distributed", true);
        }});
        this.sqlgGraph = SqlgGraph.open(configuration);
        this.sqlgGraphOpenProperty.set(true);
        for (Schema schema : this.sqlgGraph.getTopology().getSchemas()) {
            this.schemaUis.add(new SchemaUI(this, schema));
        }
        this.sqlgGraph.getTopology().registerListener((topologyInf, oldValue, action, beforeCommit) -> {
            LOGGER.info("topology listener {} {}, {}, {}", (beforeCommit ? "beforeCommit" : "afterCommit"), action.name(), topologyInf != null ? topologyInf.getClass().getSimpleName() : "-", oldValue != null ? oldValue.getClass().getSimpleName() : "-");
            switch (action) {
                case CREATE -> {
                    if (topologyInf instanceof Schema schema) {
                    } else if (topologyInf instanceof VertexLabel vertexLabel) {
                        Platform.runLater(() -> {
                            LOGGER.debug("VertexLabel creation: {}/{}/{}/{}", getGraphGroup().getName(), getName(), vertexLabel.getSchema().getName(), vertexLabel.getName());
                            this.leftPaneController.addVertexLabel(getGraphGroup(), this, vertexLabel);
                        });
                    } else if (topologyInf instanceof EdgeRole edgeRole) {
                        Platform.runLater(() -> {
                            LOGGER.debug("EdgeRole creation: {}/{}/{}/{}", getGraphGroup().getName(), getName(), edgeRole.getEdgeLabel().getSchema().getName(), edgeRole.getName());
                            this.leftPaneController.addEdgeRole(getGraphGroup(), this, edgeRole);
                        });
                    } else if (topologyInf instanceof EdgeLabel edgeLabel) {
                        Platform.runLater(() -> {
                            LOGGER.debug("EdgeLabel creation: {}/{}/{}/{}", getGraphGroup().getName(), getName(), edgeLabel.getSchema().getName(), edgeLabel.getName());
                            this.leftPaneController.addEdgeLabel(getGraphGroup(), this, edgeLabel);
                        });
                    } else if (topologyInf instanceof PropertyColumn propertyColumn) {
                        Platform.runLater(() -> {
                            LOGGER.debug("PropertyColumn creation: {}/{}/{}/{}/{}", getGraphGroup().getName(), getName(), propertyColumn.getParentLabel().getSchema().getName(), propertyColumn.getParentLabel().getName(), propertyColumn.getName());
                            AbstractLabel abstractLabel = propertyColumn.getParentLabel();
                            Schema schema = abstractLabel.getSchema();
                            this.leftPaneController.addPropertyColumn(getGraphGroup(), this, schema, abstractLabel, propertyColumn);
                        });
                    } else if (topologyInf instanceof Index ignore) {

                    }
                }
                case ADD_IN_VERTEX_LABEL_TO_EDGE -> {
                }
                case DELETE -> {
                    if (topologyInf instanceof Schema schema) {
                        Platform.runLater(() -> {
                            LOGGER.debug("Schema deletion: {}/{}/{}", getGraphGroup().getName(), getName(), schema.getName());
                            this.leftPaneController.deleteSchema(getGraphGroup(), this, schema);
                        });
                    } else if (oldValue instanceof VertexLabel oldVertexLabel) {
                        Platform.runLater(() -> {
                            LOGGER.debug("VertexLabel deletion: {}/{}/{}", getGraphGroup().getName(), getName(), oldVertexLabel.getName());
                            this.leftPaneController.deleteVertexLabel(
                                    getGraphGroup(),
                                    this,
                                    oldVertexLabel.getSchema(),
                                    oldVertexLabel
                            );
                        });
                    } else if (oldValue instanceof EdgeRole edgeRole) {
                        Schema schema = edgeRole.getEdgeLabel().getSchema();
                        Platform.runLater(() -> {
                            LOGGER.debug("EdgeRole deletion : {}/{}/{}/{}", getGraphGroup().getName(), getName(), schema.getName(), edgeRole.getName());
                            this.leftPaneController.deleteEdgeRole(
                                    getGraphGroup(),
                                    this,
                                    schema,
                                    edgeRole,
                                    edgeRole.getDirection()
                            );
                        });
                    } else if (oldValue instanceof EdgeLabel edgeLabel) {
                        Schema schema = edgeLabel.getSchema();
                        Platform.runLater(() -> {
                            LOGGER.debug("EdgeLabel deletion: {}/{}/{}", getGraphGroup().getName(), getName(), edgeLabel.getName());
                            for (EdgeRole outEdgeRole : edgeLabel.getOutEdgeRoles()) {
                                this.leftPaneController.deleteEdgeRole(
                                        getGraphGroup(),
                                        this,
                                        schema,
                                        outEdgeRole,
                                        Direction.OUT
                                );
                            }
                            for (EdgeRole inEdgeRole : edgeLabel.getInEdgeRoles()) {
                                Schema inEdgeRoleSchema = inEdgeRole.getVertexLabel().getSchema();
                                this.leftPaneController.deleteEdgeRole(
                                        getGraphGroup(),
                                        this,
                                        inEdgeRoleSchema,
                                        inEdgeRole,
                                        Direction.IN
                                );
                            }
                            this.leftPaneController.deleteEdgeLabel(
                                    getGraphGroup(),
                                    this,
                                    edgeLabel.getSchema(),
                                    edgeLabel
                            );
                        });
                    } else if (oldValue instanceof PropertyColumn propertyColumnToDelete) {
                        Platform.runLater(() -> {
                            LOGGER.debug("PropertyColumn deletion: {}/{}/{}", getGraphGroup().getName(), getName(), propertyColumnToDelete.getName());
                            AbstractLabel abstractLabel = propertyColumnToDelete.getParentLabel();
                            if (abstractLabel instanceof VertexLabel) {
                                this.leftPaneController.deletePropertyColumn(
                                        getGraphGroup(),
                                        this,
                                        abstractLabel.getSchema(),
                                        (VertexLabel) abstractLabel,
                                        null,
                                        propertyColumnToDelete
                                );
                            } else {
                                EdgeLabel edgeLabel = (EdgeLabel) abstractLabel;
                                for (VertexLabel outVertexLabel : edgeLabel.getOutVertexLabels()) {
                                    this.leftPaneController.deletePropertyColumn(
                                            getGraphGroup(),
                                            this,
                                            abstractLabel.getSchema(),
                                            outVertexLabel,
                                            (EdgeLabel) abstractLabel,
                                            propertyColumnToDelete
                                    );
                                }
                            }
                        });
                    } else if (topologyInf instanceof Index index) {
                        Platform.runLater(() -> {
                            AbstractLabel abstractLabel = index.getParentLabel();
                            LOGGER.debug("Index deletion: {}/{}/{}/{}", getGraphGroup().getName(), getName(), abstractLabel.getName(), index.getName());
                            if (abstractLabel instanceof VertexLabel) {
                                this.leftPaneController.deleteIndex(
                                        getGraphGroup(),
                                        this,
                                        abstractLabel.getSchema(),
                                        (VertexLabel) abstractLabel,
                                        null,
                                        index
                                );
                            } else {
                                EdgeLabel edgeLabel = (EdgeLabel) abstractLabel;
                                for (VertexLabel outVertexLabel : edgeLabel.getOutVertexLabels()) {
                                    this.leftPaneController.deleteIndex(
                                            getGraphGroup(),
                                            this,
                                            abstractLabel.getSchema(),
                                            outVertexLabel,
                                            (EdgeLabel) abstractLabel,
                                            index
                                    );
                                }
                            }
                        });
                    }
                }
                case UPDATE -> {
                    if (topologyInf instanceof Schema schema) {
                        Platform.runLater(() -> {
                            LOGGER.debug("Schema update : {}/{}/{}", getGraphGroup().getName(), getName(), schema.getName());
                        });
                    } else if (topologyInf instanceof VertexLabel vertexLabel) {
                        if (!vertexLabel.getName().equals(oldValue.getName())) {
                            Platform.runLater(() -> {
                                LOGGER.debug("VertexLabel update: {}/{}/{}", getGraphGroup().getName(), getName(), vertexLabel.getName());
                                this.leftPaneController.refreshTree();
                            });
                        }
                    } else if (topologyInf instanceof EdgeLabel edgeLabel) {
                        Platform.runLater(() -> {
                            LOGGER.debug("EdgeLabel update: {}/{}/{}", getGraphGroup().getName(), getName(), edgeLabel.getName());
                            this.leftPaneController.refreshTree();
                            for (VertexLabel outVertexLabel : edgeLabel.getOutVertexLabels()) {
                                this.leftPaneController.refreshEdgeLabel(
                                        getGraphGroup(),
                                        this,
                                        edgeLabel.getSchema(),
                                        outVertexLabel,
                                        (EdgeLabel) oldValue,
                                        edgeLabel
                                );
                            }
                            for (VertexLabel inVertexLabel : edgeLabel.getInVertexLabels()) {
                                this.leftPaneController.refreshEdgeLabel(
                                        getGraphGroup(),
                                        this,
                                        edgeLabel.getSchema(),
                                        inVertexLabel,
                                        (EdgeLabel) oldValue,
                                        edgeLabel
                                );
                            }
                        });
                    } else if (topologyInf instanceof PropertyColumn propertyColumn) {
                        Platform.runLater(() -> {
                            LOGGER.debug("PropertyColumn update: {}/{}/{}", getGraphGroup().getName(), getName(), propertyColumn.getName());
                            this.leftPaneController.removeOldAddUpdatedPropertyColumn(
                                    beforeCommit,
                                    getGraphGroup(),
                                    this,
                                    propertyColumn.getParentLabel().getSchema(),
                                    propertyColumn.getParentLabel(),
                                    (PropertyColumn) oldValue,
                                    propertyColumn
                            );
                            this.leftPaneController.refreshTree();
                        });
                    } else if (topologyInf instanceof Index index) {
                        Platform.runLater(() -> {
                            AbstractLabel abstractLabel = index.getParentLabel();
                            LOGGER.debug("Index update: {}/{}/{}/{}", getGraphGroup().getName(), getName(), abstractLabel.getName(), index.getName());
                            this.leftPaneController.refreshTree();
                        });
                    }
                }
            }
        });
    }

    public void refreshSqlgGraph() {
        this.schemaUis.clear();
        for (Schema schema : this.sqlgGraph.getTopology().getSchemas()) {
            this.schemaUis.add(new SchemaUI(this, schema));
        }
    }

    public void closeSqlgGraph() {
        if (this.sqlgGraph != null) {
            this.sqlgGraph.close();
            this.sqlgGraphOpenProperty.set(false);
        }
        this.sqlgGraph = null;
        this.schemaUis.clear();
    }

    public SqlgGraph getSqlgGraph() {
        return this.sqlgGraph;
    }

    public boolean isOpen() {
        return this.sqlgGraph != null;
    }

    public boolean isSqlgGraphOpenProperty() {
        return sqlgGraphOpenProperty.get();
    }

    public BooleanProperty sqlgGraphOpenPropertyProperty() {
        return sqlgGraphOpenProperty;
    }

    public LeftPaneController getLeftPaneController() {
        return this.leftPaneController;
    }
}
