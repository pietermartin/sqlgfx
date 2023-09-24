package org.sqlg.ui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.controller.PrimaryController2;
import org.umlg.sqlg.structure.Multiplicity;
import org.umlg.sqlg.structure.PropertyDefinition;
import org.umlg.sqlg.structure.PropertyType;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.IndexType;
import org.umlg.sqlg.structure.topology.PropertyColumn;
import org.umlg.sqlg.structure.topology.Schema;
import org.umlg.sqlg.structure.topology.VertexLabel;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * JavaFX App
 */
public class App extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.debug("start");
//        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
//        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
//        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
//        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        resetDb();

        PrimaryController2 primaryController2 = new PrimaryController2(stage);
        Parent parent = primaryController2.initialize();
        Scene scene = new Scene(parent);

        //noinspection DataFlowIssue
        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        //noinspection DataFlowIssue
        stage.getIcons().add(new Image(App.class.getResource("sqlg.png").toExternalForm()));
        stage.setOnCloseRequest(event -> primaryController2.close());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private void resetDb() {
        Configuration configuration = new MapConfiguration(new HashMap<>() {{
            put("jdbc.url", "jdbc:postgresql://localhost:5432/sqlgfx");
            put("jdbc.username", "postgres");
            put("jdbc.password", "postgres");
            put("distributed", true);
        }});
        try (SqlgGraph sqlgGraph = SqlgGraph.open(configuration)) {
            Set<Schema> schemas = sqlgGraph.getTopology().getSchemas();
            for (Schema schema : schemas) {
                if (!schema.getName().equals("public")) {
                    schema.remove();
                } else {
                    for (VertexLabel vertexLabel : schema.getVertexLabels().values()) {
                        vertexLabel.remove();
                    }
                }
            }
            sqlgGraph.tx().commit();
            Schema aSchema = sqlgGraph.getTopology().ensureSchemaExist("A");
            Schema bSchema = sqlgGraph.getTopology().ensureSchemaExist("B");
            VertexLabel aVertexLabel = aSchema.ensureVertexLabelExist("A", new LinkedHashMap<>() {{
                put(
                        "a1",
                        PropertyDefinition.of(
                                PropertyType.STRING,
                                Multiplicity.of(1, 1, true),
                                "'aa'",
                                "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("a1") + " <> 'a')")
                );
            }});
            PropertyColumn a1PropertyColumn = aVertexLabel.getProperty("a1").orElseThrow();
            aVertexLabel.ensureIndexExists(IndexType.UNIQUE, List.of(a1PropertyColumn));

            VertexLabel aaVertexLabel = aSchema.ensureVertexLabelExist("AA", new LinkedHashMap<>() {{
                put(
                        "a1",
                        PropertyDefinition.of(
                                PropertyType.STRING,
                                Multiplicity.of(1, 1, true),
                                "'aa'",
                                "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("a1") + " <> 'a')")
                );
            }});

            VertexLabel bVertexLabel = bSchema.ensureVertexLabelExist("B");
            aVertexLabel.ensureEdgeLabelExist(
                    "ab",
                    bVertexLabel,
                    new LinkedHashMap<>() {{
                        put(
                                "ab1",
                                PropertyDefinition.of(
                                        PropertyType.STRING,
                                        Multiplicity.of(1, 1, true),
                                        "'ab'",
                                        "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("ab1") + " <> 'a')")
                        );
                    }});
            aaVertexLabel.ensureEdgeLabelExist(
                    "ab",
                    bVertexLabel,
                    new LinkedHashMap<>() {{
                        put(
                                "ab1",
                                PropertyDefinition.of(
                                        PropertyType.STRING,
                                        Multiplicity.of(1, 1, true),
                                        "'ab'",
                                        "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("ab1") + " <> 'a')")
                        );
                    }});
            sqlgGraph.tx().commit();

            Vertex a = sqlgGraph.addVertex(T.label, "A.A");
            Vertex b = sqlgGraph.addVertex(T.label, "B.B");
            a.addEdge("ab", b);
            sqlgGraph.tx().commit();
        }

    }

}