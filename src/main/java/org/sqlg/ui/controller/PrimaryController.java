package org.sqlg.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.sqlg.ui.model.GraphConfiguration;
import org.sqlg.ui.model.GraphGroup;

import java.util.ArrayList;

public class PrimaryController {

    private Stage stage;
    @FXML
    private VBox mainVBox;
    @FXML
    private ToolBar topToolBar;
    @FXML
    private SplitPane mainSplitPane;
    @FXML
    private AnchorPane leftAnchorPane;
    @FXML
    private AnchorPane rightAnchorPane;

    private final ObservableList<GraphGroup> graphGroups = FXCollections.observableArrayList(new ArrayList<>());

    @FXML
    protected void initialize() {
//        InputStream brandsInputStream = App.class.getResourceAsStream("fontawesome/fa-brands-400.ttf");
//        InputStream regularInputStream = App.class.getResourceAsStream("fontawesome/fa-regular-400.ttf");
//        InputStream solidInputStream = App.class.getResourceAsStream("fontawesome/fa-solid-400.ttf");
//        InputStream v4InputStream = App.class.getResourceAsStream("fontawesome/fa-v4compatibility-400.ttf");
//        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesomeLocal");
//        GlyphFont glyphFont = new GlyphFont("FontAwesomeLocal", 14, solidInputStream, false);
//        Glyph graphIcon = glyphFont.create('\uf542').size(20);

    }

    public void close() {
        for (GraphGroup graphGroup : this.graphGroups) {
            for (GraphConfiguration graphConfiguration : graphGroup.getGraphConfigurations()) {
                graphConfiguration.closeSqlgGraph();
            }
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
