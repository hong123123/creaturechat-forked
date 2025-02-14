package com.owlmaddie.utils;

import net.minecraft.SharedConstants;

/**
 * The {@code VersionUtils} class is used to quickly compare the current version of Minecraft.
 */
public class VersionUtils {
    public static boolean isOlderThan(String targetVersion) {
        String currentVersion = SharedConstants.getGameVersion().getName();
        return currentVersion.compareTo(targetVersion) < 0;
    }
}