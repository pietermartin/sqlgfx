package org.sqlg.ui;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
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
import java.util.stream.Stream;

/**
 * JavaFX App
 */
public class App extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private PrimaryController primaryController;

//    @Override
    public void _start(Stage primaryStage) throws Exception {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Node Overlay Demo");
        primaryStage.show();

        HBox hBox = new HBox(new Button("One"), new Button("Two"));
        hBox.setPadding(new Insets(10));
        hBox.setSpacing(10);
        StackPane hPane = new StackPane(hBox);
        hPane.setMaxHeight(100);
        hPane.setVisible(false);
        hPane.setStyle("-fx-background-color:#55555550");

        VBox vBox = new VBox(new Button("One"), new Button("Two"));
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        StackPane vPane = new StackPane(vBox);
        vPane.setMaxWidth(100);
        vPane.setVisible(false);
        vPane.setStyle("-fx-background-color:#55555550");

        Button left = new Button("Left");
        Button top = new Button("Top");
        Button right = new Button("Right");
        Button bottom = new Button("Bottom");
        VBox buttons = new VBox(left, top, right, bottom);
        buttons.setStyle("-fx-border-width:2px;-fx-border-color:black;");
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER);
        StackPane.setMargin(buttons, new Insets(15));

        StackPane content = new StackPane(buttons);
        content.setOnMouseClicked(e -> {
            Node node = vPane.isVisible() ? vPane : hPane;
            FadeTransition ft = new FadeTransition(Duration.millis(300), node);
            ft.setOnFinished(e1 -> node.setVisible(false));
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();
        });

        root.getChildren().addAll(content, hPane, vPane);

        Stream.of(left, top, right, bottom).forEach(button -> {
            button.setOnAction(e -> {
                vPane.setVisible(false);
                hPane.setVisible(false);
                Node node;
                switch (button.getText()) {
                    case "Left":
                    case "Right":
                        node = vPane;
                        StackPane.setAlignment(vPane, button.getText().equals("Left") ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
                        break;
                    default:
                        node = hPane;
                        StackPane.setAlignment(hPane, button.getText().equals("Top") ? Pos.TOP_CENTER : Pos.BOTTOM_CENTER);
                }
                node.setVisible(true);
                FadeTransition ft = new FadeTransition(Duration.millis(300), node);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.play();
            });
        });
    }

    @SuppressWarnings("unused")
    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.debug("start");
//        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
//        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
//        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
//        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

//        resetSqlgfxDb();

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
        this.primaryController = new PrimaryController(stage, root);
        Parent parent = this.primaryController.initialize();

        Scene scene = new Scene(parent);
        scene.getRoot().setEffect(new DropShadow(10, Color.rgb(100, 100, 100)));
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.DECORATED);

        //noinspection DataFlowIssue
        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        scene.getStylesheets().add(App.class.getResource("keyword.css").toExternalForm());
        stage.setScene(scene);
        //noinspection DataFlowIssue
        stage.getIcons().add(new Image(App.class.getResource("sqlg.png").toExternalForm()));
        stage.setOnCloseRequest(ignore -> this.primaryController.close());
        stage.show();
    }

    @Override
    public void stop() {
        this.primaryController.stop();
    }

    public static void main(String[] args) {
        launch();
    }

    private void resetSqlgfxDb() {
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
                put(
                        "a2",
                        PropertyDefinition.of(
                                PropertyType.STRING,
                                Multiplicity.of(1, 1, true),
                                "'aa'",
                                "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("a2") + " <> 'a')")
                );
                put(
                        "a3",
                        PropertyDefinition.of(
                                PropertyType.INTEGER,
                                Multiplicity.of(1, 1, true),
                                "2",
                                "(" + sqlgGraph.getSqlDialect().maybeWrapInQoutes("a3") + " <> 1)")
                );
            }});
            PropertyColumn a1PropertyColumn = aVertexLabel.getProperty("a1").orElseThrow();
            PropertyColumn a2PropertyColumn = aVertexLabel.getProperty("a2").orElseThrow();
            PropertyColumn a3PropertyColumn = aVertexLabel.getProperty("a3").orElseThrow();
            aVertexLabel.ensureIndexExists(IndexType.UNIQUE, List.of(a1PropertyColumn, a2PropertyColumn, a3PropertyColumn));

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