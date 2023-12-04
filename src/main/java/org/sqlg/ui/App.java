package org.sqlg.ui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.controller.LeftPaneController;
import org.sqlg.ui.controller.PrimaryController;
import org.sqlg.ui.model.Root;
import org.umlg.sqlg.structure.Multiplicity;
import org.umlg.sqlg.structure.PropertyDefinition;
import org.umlg.sqlg.structure.PropertyType;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.topology.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * JavaFX App
 */
public class App extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    @SuppressWarnings("unused")
    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.debug("start");
//        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
//        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
//        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
//        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        resetDb();

        InputStream isOtfBrands_Regular = LeftPaneController.class.getResourceAsStream("/org/sqlg/ui/images/kit-1f0259751e-desktop/otfs/Font Awesome 6 Brands-Regular-400.otf");
        assert isOtfBrands_Regular != null;
        Font fontBrands_Regular = Font.loadFont(isOtfBrands_Regular, -1);
        InputStream isOtfDuotone_Solid = LeftPaneController.class.getResourceAsStream("/org/sqlg/ui/images/kit-1f0259751e-desktop/otfs/Font Awesome 6 Duotone-Solid-900.otf");
        assert isOtfDuotone_Solid != null;
        Font fontDuotone_Solid = Font.loadFont(isOtfDuotone_Solid, -1);
        InputStream isOtfLight = LeftPaneController.class.getResourceAsStream("/org/sqlg/ui/images/kit-1f0259751e-desktop/otfs/Font Awesome 6 Pro-Light-300.otf");
        assert isOtfLight != null;
        Font fontLight = Font.loadFont(isOtfLight, -1);
        InputStream isOtfRegular = LeftPaneController.class.getResourceAsStream("/org/sqlg/ui/images/kit-1f0259751e-desktop/otfs/Font Awesome 6 Pro-Regular-400.otf");
        assert isOtfRegular != null;
        Font fontRegular = Font.loadFont(isOtfRegular, -1);
        InputStream isOtfSolid = LeftPaneController.class.getResourceAsStream("/org/sqlg/ui/images/kit-1f0259751e-desktop/otfs/Font Awesome 6 Pro-Solid-900.otf");
        assert isOtfSolid != null;
        Font fontSolid = Font.loadFont(isOtfSolid, -1);
        InputStream isOtfThin = LeftPaneController.class.getResourceAsStream("/org/sqlg/ui/images/kit-1f0259751e-desktop/otfs/Font Awesome 6 Pro-Thin-100.otf");
        assert isOtfThin != null;
        Font fontThin = Font.loadFont(isOtfThin, -1);
        InputStream isOtfSharp_Light = LeftPaneController.class.getResourceAsStream("/org/sqlg/ui/images/kit-1f0259751e-desktop/otfs/Font Awesome 6 Sharp-Light-300.otf");
        assert isOtfSharp_Light != null;
        Font fontSharp_Light = Font.loadFont(isOtfSharp_Light, -1);
        InputStream isOtfSharp_Regular = LeftPaneController.class.getResourceAsStream("/org/sqlg/ui/images/kit-1f0259751e-desktop/otfs/Font Awesome 6 Sharp-Regular-400.otf");
        assert isOtfSharp_Regular != null;
        Font fontSharp_Regular = Font.loadFont(isOtfSharp_Regular, -1);
        InputStream isOtfSharp_Solid = LeftPaneController.class.getResourceAsStream("/org/sqlg/ui/images/kit-1f0259751e-desktop/otfs/Font Awesome 6 Sharp-Solid-900.otf");
        assert isOtfSharp_Solid != null;
        Font fontSharp_Solid = Font.loadFont(isOtfSharp_Solid, -1);

        Root root = new Root();
        PrimaryController primaryController = new PrimaryController(stage, root);
        Parent parent = primaryController.initialize();

        Scene scene = new Scene(parent);
//        scene.getRoot().setEffect(new DropShadow(10, Color.rgb(100, 100, 100)));
//        scene.setFill(Color.TRANSPARENT);
//        stage.initStyle(StageStyle.DECORATED);

        //noinspection DataFlowIssue
        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        //noinspection DataFlowIssue
        stage.getIcons().add(new Image(App.class.getResource("sqlg.png").toExternalForm()));
        stage.setOnCloseRequest(ignore -> primaryController.close());
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

            VertexLabel cVertexLabel = aSchema.ensurePartitionedVertexLabelExist("C",
                    new LinkedHashMap<>() {{
                        put("name", PropertyDefinition.of(PropertyType.STRING));
                        put("part1", PropertyDefinition.of(PropertyType.INTEGER));
                        put("part2", PropertyDefinition.of(PropertyType.INTEGER));
                        put("part3", PropertyDefinition.of(PropertyType.INTEGER));
                        put("other", PropertyDefinition.of(PropertyType.STRING));
                    }},
                    ListOrderedSet.listOrderedSet(List.of("name", "part1", "part2", "part3")),
                    PartitionType.LIST,
                    "\"part1\""
            );
            Partition part1_1 = cVertexLabel.ensureListPartitionWithSubPartitionExists("part1_1", "'1'", PartitionType.LIST, "\"part2\"");
            Partition part1_2 = cVertexLabel.ensureListPartitionWithSubPartitionExists("part1_2", "'2'", PartitionType.LIST, "\"part2\"");

            Partition part1_1_1 = part1_1.ensureListPartitionWithSubPartitionExists("part1_1_1", "1", PartitionType.LIST, "\"part3\"");
            part1_1_1.ensureListPartitionExists("part1_1_1_1", "1");
            part1_1_1.ensureListPartitionExists("part1_1_1_2", "2");
            Partition part1_1_2 = part1_1.ensureListPartitionWithSubPartitionExists("part1_1_2", "2", PartitionType.LIST, "\"part3\"");
            part1_1_2.ensureListPartitionExists("part1_1_2_1", "1");
            part1_1_2.ensureListPartitionExists("part1_1_2_2", "2");


            Partition part1_2_1 = part1_2.ensureListPartitionWithSubPartitionExists("part1_2_1", "1", PartitionType.LIST, "\"part3\"");
            Partition part1_2_2 = part1_2.ensureListPartitionWithSubPartitionExists("part1_2_2", "2", PartitionType.LIST, "\"part3\"");
            part1_2_1.ensureListPartitionExists("part1_2_1_1", "1");
            part1_2_2.ensureListPartitionExists("part1_2_2_1", "1");

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