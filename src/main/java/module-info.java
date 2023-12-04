module org.sqlg {

    requires org.slf4j;
    requires org.apache.logging.log4j.slf4j;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires org.fxmisc.undo;

    requires org.antlr.antlr4.runtime;

    requires org.controlsfx.controls;

//    requires atlantafx.base;

    requires net.synedra.validatorfx;

    requires java.sql;
    requires org.apache.commons.configuration2;
    requires com.fasterxml.jackson.databind;

    requires gremlin.core;
    requires gremlin.language;
    requires gremlin.shaded;

    requires sqlg.core;
    requires sqlg.c3p0;
    requires sqlg.postgres.dialect;

    requires org.apache.commons.collections4;

    exports org.sqlg.ui;
    opens org.sqlg.ui to javafx.fxml;
    exports org.sqlg.ui.model;
    opens org.sqlg.ui.model to javafx.fxml;
    exports org.sqlg.ui.controller;
    opens org.sqlg.ui.controller to javafx.fxml;
}