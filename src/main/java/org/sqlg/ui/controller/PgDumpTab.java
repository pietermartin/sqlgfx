package org.sqlg.ui.controller;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.model.GraphConfiguration;
import org.umlg.sqlg.structure.TopologyInf;
import org.umlg.sqlg.structure.topology.EdgeLabel;
import org.umlg.sqlg.structure.topology.VertexLabel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PgDumpTab extends BaseController {

    private final Logger LOGGER = LoggerFactory.getLogger(PgDumpTab.class);
    private final BorderPane borderPane;
    private final CodeArea pgDumpResult;

    public PgDumpTab(Stage stage) {
        super(stage);
        this.borderPane = new BorderPane();
        this.pgDumpResult = new CodeArea();
        this.pgDumpResult.setEditable(false);
        this.pgDumpResult.setParagraphGraphicFactory(LineNumberFactory.get(this.pgDumpResult));
        VirtualizedScrollPane<CodeArea> resultVirtualizedScrollPane = new VirtualizedScrollPane<>(this.pgDumpResult);
        this.borderPane.setCenter(resultVirtualizedScrollPane);
    }

    public void updateView(GraphConfiguration graphConfiguration, TopologyInf topologyInf) {
        this.pgDumpResult.clear();
        try {
            if (topologyInf instanceof VertexLabel || topologyInf instanceof EdgeLabel) {

                String url = graphConfiguration.getUrl();
                String[] parts = url.split(":");
                if (parts.length == 4) {

                    String host = parts[2];
                    host = StringUtils.removeStart(host, "/");
                    host = StringUtils.removeStart(host, "/");

                    String db = parts[3];
                    db = db.substring(db.indexOf("/") + 1);
                    String jdbcUser = graphConfiguration.getJdbcUser();
                    String jdbcPassword = graphConfiguration.getJdbcPassword();

                    ProcessBuilder processBuilder = new ProcessBuilder();
                    processBuilder.redirectErrorStream(true);
                    String table;
                    if (topologyInf instanceof VertexLabel vertexLabel) {
                        table = String.format("\"%s\".\"V_%s\"", vertexLabel.getSchema().getName(), vertexLabel.getName());
                    } else {
                        EdgeLabel edgeLabel = (EdgeLabel)topologyInf;
                        table = String.format("\"%s\".\"E_%s\"", edgeLabel.getSchema().getName(), edgeLabel.getName());
                    }
                    processBuilder.command("pg_dump", "-h", host, "-U", jdbcUser, "-d", db, "-s", "-t", table);
                    var process = processBuilder.start();
                    this.pgDumpResult.appendText(processBuilder.command().toString());
                    this.pgDumpResult.appendText("\n");
                    try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            this.pgDumpResult.appendText(line);
                            this.pgDumpResult.appendText("\n");
                        }
                    }
                    this.pgDumpResult.appendText(String.valueOf(process.waitFor()));
                    this.pgDumpResult.appendText("\n");

                } else {
                    this.pgDumpResult.appendText("Expected 4 parts, found, " + parts);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Parent getView() {
        return this.borderPane;
    }
}
