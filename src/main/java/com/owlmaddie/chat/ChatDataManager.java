package com.owlmaddie.chat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.owlmaddie.network.ServerPackets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code ChatDataManager} class manages chat data for all entities. This class also helps
 * generate new messages, set entity goals, and other useful chat-related functions.
 */
public class ChatDataManager {
    // Use a static instance to manage our data globally
    private static final ChatDataManager SERVER_INSTANCE = new ChatDataManager(true);
    private static final ChatDataManager CLIENT_INSTANCE = new ChatDataManager(false);
    public static final Logger LOGGER = LoggerFactory.getLogger("creaturechat");
    public static int MAX_CHAR_PER_LINE = 20;
    public static int DISPLAY_NUM_LINES = 3;
    public static int MAX_CHAR_IN_USER_MESSAGE = 512;
    public static int TICKS_TO_DISPLAY_USER_MESSAGE = 70;
    public static int MAX_AUTOGENERATE_RESPONSES = 3;
    private static final Gson GSON = new Gson();

    public enum ChatStatus {
        NONE,       // No chat status yet
        PENDING,    // Chat is pending (e.g., awaiting response or processing)
        DISPLAY,    // Chat is currently being displayed
        HIDDEN,     // Chat is currently hidden
    }

    public enum ChatSender {
        USER,      // A user chat message
        ASSISTANT  // A GPT generated message
    }

    // HashMap to associate unique entity IDs with their chat data
    public ConcurrentHashMap<String, EntityChatData> entityChatDataMap;

    public void clearData() {
        // Clear the chat data for the previous session
        entityChatDataMap.clear();
    }

    private ChatDataManager(Boolean server_only) {
        // Constructor
        entityChatDataMap = new ConcurrentHashMap<>();

        if (server_only) {
            // Generate initial quest
            // TODO: Complete the quest flow
            //generateQuest();
        }
    }

    // Method to get the global instance of the server data manager
    public static ChatDataManager getServerInstance() {
        return SERVER_INSTANCE;
    }

    // Method to get the global instance of the client data manager (synced from server)
    public static ChatDataManager getClientInstance() {
        return CLIENT_INSTANCE;
    }

    // Retrieve chat data for a specific entity, or create it if it doesn't exist
    public EntityChatData getOrCreateChatData(String entityId) {
        return entityChatDataMap.computeIfAbsent(entityId, k -> new EntityChatData(entityId));
    }

    // Update the UUID in the map (i.e. bucketed entity and then released, changes their UUID)
    public void updateUUID(String oldUUID, String newUUID) {
        EntityChatData data = entityChatDataMap.remove(oldUUID);
        if (data != null) {
            data.entityId = newUUID;
            entityChatDataMap.put(newUUID, data);
            LOGGER.info("Updated chat data from UUID (" + oldUUID + ") to UUID (" + newUUID + ")");

            // Broadcast to all players
            ServerPackets.BroadcastEntityMessage(data);
        } else {
            LOGGER.info("Unable to update chat data, UUID not found: " + oldUUID);
        }
    }

    // Save chat data to file
    public String GetLightChatData(String playerName) {
        try {
            // Create "light" version of entire chat data HashMap
            HashMap<String, EntityChatDataLight> lightVersionMap = new HashMap<>();
            this.entityChatDataMap.forEach((name, entityChatData) -> lightVersionMap.put(name, entityChatData.toLightVersion(playerName)));
            return GSON.toJson(lightVersionMap);
        } catch (Exception e) {
            // Handle exceptions
            return "";
        }
    }

    // Save chat data to file
    public void saveChatData(MinecraftServer server) {
        File saveFile = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), "chatdata.json");
        LOGGER.info("Saving chat data to " + saveFile.getAbsolutePath());

        // Clean up blank, temp entities in data
        entityChatDataMap.values().removeIf(entityChatData -> entityChatData.status == ChatStatus.NONE);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(saveFile), StandardCharsets.UTF_8)) {
            GSON.toJson(this.entityChatDataMap, writer);
        } catch (Exception e) {
            String errorMessage = "Error saving `chatdata.json`. No CreatureChat chat history was saved! " + e.getMessage();
            LOGGER.error(errorMessage, e);
            ServerPackets.sendErrorToAllOps(server, errorMessage);
        }
    }

    // Load chat data from file
    public void loadChatData(MinecraftServer server) {
        File loadFile = new File(server.getSavePath(WorldSavePath.ROOT).toFile(), "chatdata.json");
        LOGGER.info("Loading chat data from " + loadFile.getAbsolutePath());

        if (loadFile.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(loadFile), StandardCharsets.UTF_8)) {
                Type type = new TypeToken<ConcurrentHashMap<String, EntityChatData>>(){}.getType();
                this.entityChatDataMap = GSON.fromJson(reader, type);

                // Clean up blank, temp entities in data
                entityChatDataMap.values().removeIf(entityChatData -> entityChatData.status == ChatStatus.NONE);

                // Post-process each EntityChatData object
                for (EntityChatData entityChatData : entityChatDataMap.values()) {
                    entityChatData.postDeserializeInitialization();
                }
            } catch (Exception e) {
                LOGGER.error("Error loading chat data", e);
                this.entityChatDataMap = new ConcurrentHashMap<>();
            }
        } else {
            // Init empty chat data
            this.entityChatDataMap = new ConcurrentHashMap<>();
        }
    }
}