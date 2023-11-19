package org.sqlg.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

public enum Fontawesome {

    COLUMNS('\ue361'),
    LAYER_GROUP('\uf5fd'),
    LIST_UL('\uf0ca'),
    OBJECT_COLUMN('\ue3c1'),
    TABLE('\uf0ce'),
    TABLE_COLUMNS('\uf0db'),
    CODE_MERGE('\uf387'),
    EDGE_LEGACY('\ue078'),
    EDGE('\uf282'),
    CHART_SIMPLE_HORIZONTAL('\ue474'),
    ARROW_RIGHT('\uf061'),
    ARROW_LEFT('\uf060'),
    INDENT('\uf03c'),
    BLOCK_QUOTE('\ue0b5'),
    DATABASE('\uf1c0');
    final char unicode;

    Fontawesome(char unicode) {
        this.unicode = unicode;
    }

    public String unicode() {
        return String.valueOf(this.unicode);
    }

    public Label label() {
        Label label = new Label();
        label.getStyleClass().add("fontawesome");
        label.setText(this.unicode());
        label.setPadding(Insets.EMPTY);
        label.setAlignment(Pos.CENTER_LEFT);
        return label;
    }
}
