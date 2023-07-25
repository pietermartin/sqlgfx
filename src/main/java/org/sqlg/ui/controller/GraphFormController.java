package org.sqlg.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.converter.DefaultStringConverter;
import org.sqlg.ui.model.GraphConfiguration;

public class GraphFormController {

    @FXML
    private TextField graphFormNameTxt;
    @FXML
    private TextField graphFormUrlTxt;
    @FXML
    private TextField graphFormUsernameTxt;
    @FXML
    private PasswordField graphFormPasswordTxt;
    private final LeftPaneController leftPaneController;
    private final GraphConfiguration graphConfiguration;

    public GraphFormController(LeftPaneController leftPaneController, GraphConfiguration graphConfiguration) {
        this.graphConfiguration = graphConfiguration;
        this.leftPaneController = leftPaneController;
    }

    @FXML
    protected void initialize() {
        Bindings.bindBidirectional(this.graphFormNameTxt.textProperty(), this.graphConfiguration.nameProperty(), new DefaultStringConverter());
        Bindings.bindBidirectional(this.graphFormUrlTxt.textProperty(), this.graphConfiguration.urlProperty(), new DefaultStringConverter());
        Bindings.bindBidirectional(this.graphFormUsernameTxt.textProperty(), this.graphConfiguration.usernameProperty(), new DefaultStringConverter());
        Bindings.bindBidirectional(this.graphFormPasswordTxt.textProperty(), this.graphConfiguration.passwordProperty(), new DefaultStringConverter());

        this.graphFormNameTxt.textProperty().addListener((observable, oldValue, newValue) -> {
            leftPaneController.refreshTree();
        });
    }
}
