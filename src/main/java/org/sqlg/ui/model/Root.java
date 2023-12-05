package org.sqlg.ui.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Root {

    private final ObjectMapper objectMapper;
    private final Path configPath;
    private final ListOrderedSet<User> users = new ListOrderedSet<>();

    public Root() {
        this.objectMapper = new ObjectMapper();
        String homeProp = System.getProperty("user.home");
        this.configPath = new File(homeProp).toPath().resolve(".sqlgfx");
        //noinspection ResultOfMethodCallIgnored
        this.configPath.toFile().mkdir();
        ListOrderedSet<User> users = readConfig();
        if (users.isEmpty()) {
            User fake = new User(this, "fake", null);
            fake.getGraphGroups().add(new GraphGroup(fake, "default"));
            users.add(fake);
        }
        this.users.addAll(users);
    }

    public ListOrderedSet<User> getUsers() {
        return users;
    }

    public ListOrderedSet<User> readConfig() {
        if (this.configPath.toFile().exists()) {
            Path configurationJson = this.configPath.resolve("configuration.json");
            if (configurationJson.toFile().exists()) {
                try {
                    ObjectNode configuration = (ObjectNode) objectMapper.readTree(configurationJson.toFile());
                    ArrayNode usersArrayNode = (ArrayNode) configuration.get("users");
                    for (JsonNode userJson : usersArrayNode) {
                        ObjectNode userObjectNode = (ObjectNode) userJson;
                        String username = userJson.get("username").asText();
                        User user = new User(this, username, null);
                        this.users.add(user);

                        user.fromJson(userObjectNode);

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return this.users;
    }

    public void persistConfig() {
        Path configurationJsonPath = this.configPath.resolve("configuration.json");
        ObjectNode json = this.objectMapper.createObjectNode();
        ArrayNode usersArrayNode = this.objectMapper.createArrayNode();
        json.set("users", usersArrayNode);
        for (User user : this.users) {
            if (!user.getUsername().equals("fake")) {
                ObjectNode userObjectNode = user.toJson(this.objectMapper);
                usersArrayNode.add(userObjectNode);
            }
        }
        try {
            Files.write(configurationJsonPath, json.toPrettyString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
