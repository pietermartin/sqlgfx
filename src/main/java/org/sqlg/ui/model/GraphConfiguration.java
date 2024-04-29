package org.sqlg.ui.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlg.ui.AESUtil;
import org.umlg.sqlg.structure.SqlgDataSource;
import org.umlg.sqlg.structure.SqlgDataSourceFactory;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.structure.TopologyListener;
import org.umlg.sqlg.structure.topology.Schema;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public final class GraphConfiguration implements ISqlgTopologyUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphConfiguration.class);
    private SqlgGraph sqlgGraph;
    private final BooleanProperty sqlgGraphOpenProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty refreshTopologyProperty = new SimpleBooleanProperty(false);
    private final GraphGroup graphGroup;
    private final ObservableList<SchemaUI> schemaUis = FXCollections.observableArrayList(new ArrayList<>());
    private final static String ALGORHYTHM = "AES/CBC/PKCS5Padding";

    public enum TESTED {
        UNTESTED,
        FALSE,
        TRUE;
    }

    public static final String USERNAME = "username";
    private final StringProperty username = new SimpleStringProperty(this, USERNAME);
    public static final String NAME = "name";
    private final StringProperty name = new SimpleStringProperty(this, NAME);
    public static final String URL = "url";
    private final StringProperty url = new SimpleStringProperty(this, URL);
    public static final String JDBC_USER = "jdbcUser";
    private final StringProperty jdbcUser = new SimpleStringProperty(this, JDBC_USER);
    public static final String JDBC_PASSWORD = "jdbcPassword";
    private final StringProperty jdbcPassword = new SimpleStringProperty(this, JDBC_PASSWORD);
    public static final String SAVE_PASSWORD = "savePassword";
    private final BooleanProperty savePassword = new SimpleBooleanProperty(this, SAVE_PASSWORD);
    public static final String _TESTED = "tested";
    private final ObjectProperty<TESTED> tested = new SimpleObjectProperty<>(TESTED.UNTESTED, _TESTED);

    public GraphConfiguration(
            String username,
            GraphGroup graphGroup,
            String name,
            String url,
            String jdbcUser,
            boolean savePassword,
            String jdbcPassword,
            TESTED tested) {

        assert Objects.nonNull(graphGroup) : "graphGroup may not be null";
        this.graphGroup = graphGroup;
        this.username.set(username);
        this.name.set(name);
        this.url.set(url);
        this.jdbcUser.set(jdbcUser);
        this.savePassword.set(savePassword);
        this.jdbcPassword.set(jdbcPassword);
        this.tested.set(tested);

    }

    public GraphGroup getGraphGroup() {
        return graphGroup;
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public String getJdbcUser() {
        return jdbcUser.get();
    }

    public StringProperty jdbcUserProperty() {
        return jdbcUser;
    }

    public void setJdbcUser(String jdbcUser) {
        this.jdbcUser.set(jdbcUser);
    }

    public String getJdbcPassword() {
        return jdbcPassword.get();
    }

    public StringProperty jdbcPasswordProperty() {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword.set(jdbcPassword);
    }

    public boolean isSavePassword() {
        return savePassword.get();
    }

    public BooleanProperty savePasswordProperty() {
        return savePassword;
    }

    public void setSavePassword(boolean savePassword) {
        this.savePassword.set(savePassword);
    }

    public TESTED getTested() {
        return tested.get();
    }

    public ObjectProperty<TESTED> testedProperty() {
        return tested;
    }

    public void setTested(TESTED tested) {
        this.tested.set(tested);
    }

    public ObservableList<SchemaUI> getSchemaUis() {
        return this.schemaUis;
    }

    public void testGraphConnection() {
        SqlgDataSource dataSource = null;
        try {
            Configuration configuration = new MapConfiguration(new HashMap<>() {{
                put("jdbc.url", getUrl());
                put("jdbc.username", getJdbcUser());
                put("jdbc.password", getJdbcPassword());
            }});
            dataSource = SqlgDataSourceFactory.create(configuration);
            dataSource.getDatasource().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

    public void openSqlgGraph(TopologyListener topologyListener) {
        Configuration configuration = new MapConfiguration(new HashMap<>() {{
            put("jdbc.url", getUrl());
            put("jdbc.username", getJdbcUser());
            put("jdbc.password", getJdbcPassword());
            put("distributed", true);
        }});
        this.sqlgGraph = SqlgGraph.open(configuration);
        this.sqlgGraphOpenProperty.set(true);
        for (Schema schema : this.sqlgGraph.getTopology().getSchemas()) {
            this.schemaUis.add(new SchemaUI(this, schema));
        }
        this.sqlgGraph.getTopology().registerListener(topologyListener);
    }

    public void refreshSqlgGraph() {
        this.schemaUis.clear();
        for (Schema schema : this.sqlgGraph.getTopology().getSchemas()) {
            this.schemaUis.add(new SchemaUI(this, schema));
        }
    }

    public void closeSqlgGraph() {
        if (!this.isSavePassword()) {
            setJdbcPassword(null);
        }
        if (this.sqlgGraph != null) {
            this.sqlgGraph.close();
            this.sqlgGraphOpenProperty.set(false);
        }
        this.sqlgGraph = null;
        this.schemaUis.clear();
    }

    public SqlgGraph getSqlgGraph() {
        return this.sqlgGraph;
    }

    public boolean isOpen() {
        return this.sqlgGraph != null;
    }

    public boolean isSqlgGraphOpenProperty() {
        return sqlgGraphOpenProperty.get();
    }

    public BooleanProperty sqlgGraphOpenPropertyProperty() {
        return sqlgGraphOpenProperty;
    }

    public boolean isRefreshTopologyProperty() {
        return refreshTopologyProperty.get();
    }

    public BooleanProperty refreshTopologyPropertyProperty() {
        return refreshTopologyProperty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphConfiguration that = (GraphConfiguration) o;
        return Objects.equals(graphGroup, that.graphGroup) && Objects.equals(name, that.name) && Objects.equals(url, that.url) && Objects.equals(jdbcUser, that.jdbcUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(graphGroup, name.get(), url.get(), jdbcUser.get());
    }

    @Override
    public String toString() {
        return STR."GraphConfiguration{name=\{name.get()}, url=\{url.get()}, username=\{jdbcUser.get()}\{'}'}";
    }

    public ObjectNode toJson(ObjectMapper objectMapper) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("name", getName());
        objectNode.put("url", getUrl());
        objectNode.put("jdbcUser", getJdbcUser());
        if (isSavePassword()) {
            String password = getJdbcPassword();
            try {
                SecretKey key = AESUtil.getKeyFromPassword(getGraphGroup().getUser().getPassword(), AESUtil.salt);
                IvParameterSpec ivParameterSpec = AESUtil.generateIv();
                String cipherPassword = AESUtil.encrypt(ALGORHYTHM, password, key, ivParameterSpec);
                String ivAsString = Arrays.toString(ivParameterSpec.getIV());
                objectNode.put("jdbcPassword", ivAsString + cipherPassword);
                String plainText = AESUtil.decrypt(ALGORHYTHM, cipherPassword, key, ivParameterSpec);
                assert password.equals(plainText);
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                     IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                     InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
        return objectNode;
    }

    public static GraphConfiguration fromJson(User user, GraphGroup graphGroup, ObjectNode graphConfigurationJson) {
        String name = graphConfigurationJson.get("name").asText();
        String url = graphConfigurationJson.get("url").asText();
        String jdbcUser = graphConfigurationJson.get("jdbcUser").asText();
        String jdbcPassword = null;
        if (graphConfigurationJson.hasNonNull("jdbcPassword")) {
            jdbcPassword = graphConfigurationJson.get("jdbcPassword").asText();
        }
        return new GraphConfiguration(
                user.getUsername(),
                graphGroup,
                name,
                url,
                jdbcUser,
                jdbcPassword != null,
                jdbcPassword,
                GraphConfiguration.TESTED.UNTESTED
        );
    }

    public void decryptPasswords() {
        if (this.isSavePassword()) {
            try {
                int indexOf = this.jdbcPassword.get().indexOf("]");
                String ivBytes = this.jdbcPassword.get().substring(1, indexOf);
                String[] parts = ivBytes.split(",");
                byte[] iv = new byte[parts.length];
                for (int i = 0; i < iv.length; i++) {
                    iv[i] = Byte.parseByte(parts[i].trim());
                }
                String jdbcPasswordPassword = this.jdbcPassword.get().substring(indexOf + 1);
                IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                SecretKey key = AESUtil.getKeyFromPassword(getGraphGroup().getUser().getPassword(), AESUtil.salt);
                String plainTextPassword = AESUtil.decrypt(ALGORHYTHM, jdbcPasswordPassword, key, ivParameterSpec);
                this.jdbcPassword.set(plainTextPassword);
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                     IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                     InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
