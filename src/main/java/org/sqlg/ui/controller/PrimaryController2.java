package org.sqlg.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.GraphConfiguration;
import org.sqlg.ui.model.GraphGroup;

import java.util.ArrayList;

public class PrimaryController2 {

    private Stage stage;
    private static final Logger LOGGER = LoggerFactory.getLogger(PrimaryController2.class);
    private final ObservableList<GraphGroup> graphGroups = FXCollections.observableArrayList(new ArrayList<>());

    public PrimaryController2(Stage stage) {
        this.stage = stage;
    }

    public Parent initialize() {
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefHeight(600D);
        borderPane.setPrefWidth(1100D);
        ToolBar toolbar = new ToolBar();
        toolbar.setMinHeight(40D);
        borderPane.setTop(toolbar);

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0, 0.25D);
        borderPane.setCenter(splitPane);
        AnchorPane anchorPaneLeft = new AnchorPane();
        AnchorPane anchorPaneRight = new AnchorPane();
        splitPane.getItems().addAll(anchorPaneLeft, anchorPaneRight);
        StatusBar statusbar = new StatusBar();
        borderPane.setBottom(statusbar);

        LeftPaneController leftPaneController = new LeftPaneController(
                this,
                this.graphGroups,
                anchorPaneLeft,
                anchorPaneRight
        );

        GraphGroup graphGroup = new GraphGroup("Default");
        this.graphGroups.add(graphGroup);
        graphGroup.add(new GraphConfiguration(leftPaneController, graphGroup, "graph1", "jdbc:postgresql://localhostx:5432/sqlgfx", "postgres", "postgres", GraphConfiguration.TESTED.UNTESTED));
        graphGroup.add(new GraphConfiguration(leftPaneController, graphGroup, "graph2", "jdbc:postgresql://localhost:5432/sqlgfx", "postgres", "postgres", GraphConfiguration.TESTED.UNTESTED));
        graphGroup.add(new GraphConfiguration(leftPaneController, graphGroup, "graph3", "jdbc:postgresql://localhost:5432/sqlgfx", "postgres", "postgres", GraphConfiguration.TESTED.UNTESTED));
        leftPaneController.initialize();

        BreadCrumbBar<String> breadCrumbBar = new BreadCrumbBar<>();
        toolbar.getItems().add(breadCrumbBar);
        TreeItem<String> leaf = new TreeItem<>("New Crumb #");
        breadCrumbBar.setSelectedCrumb(leaf);
        return borderPane;
    }

    public Stage getStage() {
        return stage;
    }

    public void close() {
        for (GraphGroup graphGroup : this.graphGroups) {
            for (GraphConfiguration graphConfiguration : graphGroup.getGraphConfigurations()) {
                graphConfiguration.closeSqlgGraph();
            }
        }
    }
}
