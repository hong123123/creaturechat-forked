package com.owlmaddie.chat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code ChatPrompt} class is used to load a prompt from the Minecraft resource manager
 */
public class ChatPrompt {
    public static final Logger LOGGER = LoggerFactory.getLogger("creaturechat");

    // This method should be called in an appropriate context where ResourceManager is available
    public static String loadPromptFromResource(ResourceManager resourceManager, String promptName) {
        Identifier fileIdentifier = new Identifier("creaturechat", "prompts/" + promptName);
        try (InputStream inputStream = resourceManager.getResource(fileIdentifier).get().getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            return contentBuilder.toString();
        } catch (Exception e) {
            LOGGER.error("Failed to read prompt file", e);
        }
        return null;
    }
}
