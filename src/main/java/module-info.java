module org.sqlg {

    requires org.slf4j;
    requires org.apache.logging.log4j.slf4j;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    requires atlantafx.base;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.boxicons;
    requires net.synedra.validatorfx;

    requires java.sql;
    requires org.apache.commons.configuration2;

    requires gremlin.core;
//    requires gremlin.language;
    requires gremlin.shaded;

    requires sqlg.core;
    requires sqlg.c3p0;
    requires sqlg.postgres.dialect;

    exports org.sqlg.ui;
    opens org.sqlg.ui to javafx.fxml;
    exports org.sqlg.ui.model;
    opens org.sqlg.ui.model to javafx.fxml;
    exports org.sqlg.ui.controller;
    opens org.sqlg.ui.controller to javafx.fxml;
}
