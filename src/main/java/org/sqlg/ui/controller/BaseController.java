package org.sqlg.ui.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.ExceptionDialog;

import java.util.function.Consumer;

public class BaseController {

    private final Stage stage;

    public BaseController(Stage stage) {
        this.stage = stage;
    }

    protected void showDialog(Alert.AlertType alertType, String headerText, String text) {
        showDialog(alertType, headerText, text, null, null);
    }

    protected void showDialog(
            Alert.AlertType alertType,
            String headerText,
            String text,
            Exception e,
            Consumer<Object> onClose) {

        Dialog<?> alert;
        if (alertType == Alert.AlertType.ERROR && e != null) {
            alert = new ExceptionDialog(e);
        } else {
            alert = new Alert(alertType, text);
        }
        alert.initModality(Modality.NONE);
        alert.initOwner(stage);
        alert.getDialogPane().setHeaderText(headerText);
        alert.getDialogPane().setMinWidth(400);
        alert.getDialogPane().setMinHeight(200);
        alert.setX(stage.getX() + stage.getWidth() - 410);
        alert.setY(stage.getY() + 10);
        if (alertType != Alert.AlertType.ERROR) {
            new Thread(() -> {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                Platform.runLater(alert::close);
            }).start();
        }
        if (onClose != null) {
            alert.showAndWait().ifPresent(onClose);
        } else {
            alert.showAndWait();
        }
    }
}
