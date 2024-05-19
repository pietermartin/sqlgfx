package org.sqlg.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

public enum Fontawesome {

    COLUMNS('\ue361'),
    LAYER_GROUP('\uf5fd'),
    BARS('\uf0c9'),
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
    PLUS('+'),
    MINUS('-'),
    SQUARE('\uf0c8'),
    SQUARE_XMARK('\uf2d3'),
    XMARK('\uf00d'),
    DATABASE('\uf1c0'),
    SERVER('\uf233'),
    ARROWS_ROTATE_RIGHT('\uf01e'),
    SPLIT('\ue254'),
    FILE_DASHED_LINE('\uf877'),
    PLAY('\uf04b'),
    STOP('\uf04d'),
    SPINNER('\uf110'),
    MAGNIFYING_GLASS('\uf002'),
    GEAR('\uf013'),
    CIRCLE_INFO('\uf05a'),
    SQUARE_QUESTION('\uf2fd'),
    LINES_LEANING('\ue51e'),
    RECTANGLE_HISTORY('\ue4a2'),
    STREET_VIEW('\uf21d'),
    WRENCH('\uf0ad'),
    GEARS('\uf085');
    final char unicode;

    Fontawesome(char unicode) {
        this.unicode = unicode;
    }

    public String unicode() {
        return String.valueOf(this.unicode);
    }

    public enum Type {
        Brands_Regular,
        Duotone_Solid,
        Light,
        Regular,
        Solid,
        Thin,
        Sharp_Light,
        Sharp_Regular,
        Sharp_Solid,
    }

    public Label label(Type type, int fontSize) {
        return label(type, fontSize, null);
    }

    public Label label(Type type) {
        return label(type, 14);
    }

    public Label label(Type type, int fontSize, String style) {
        Label label = new Label();
        label.getStyleClass().add("fontawesome");
        if (style != null) {
            label.getStyleClass().add(style);
        }
        label.setStyle("-fx-font-size: " + fontSize);

        label.setText(this.unicode());
//        label.setText("AAA");
//        label.setPadding(Insets.EMPTY);
        label.setAlignment(Pos.CENTER_LEFT);
//        label.setTextAlignment(TextAlignment.RIGHT);
//        label.setAlignment(Pos.CENTER_LEFT);
//        label.setAlignment(Pos.CENTER_RIGHT);
        switch (type) {
            case Brands_Regular -> label.getStyleClass().add("fontawesome-Brands_Regular");
            case Duotone_Solid -> label.getStyleClass().add("fontawesome-Duotone_Solid");
            case Light -> label.getStyleClass().add("fontawesome-Light");
            case Regular -> label.getStyleClass().add("fontawesome-Regular");
            case Solid -> label.getStyleClass().add("fontawesome-Solid");
            case Thin -> label.getStyleClass().add("fontawesome-Thin");
            case Sharp_Light -> label.getStyleClass().add("fontawesome-Sharp_Light");
            case Sharp_Regular -> label.getStyleClass().add("fontawesome-Sharp_Regular");
            case Sharp_Solid -> label.getStyleClass().add("fontawesome-Sharp_Solid");
        }
        return label;
    }

}
