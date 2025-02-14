package com.owlmaddie.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

/**
 * The {@code ServerEntityFinder} class is used to find a specific LivingEntity by UUID, since
 * there is not a built-in method for this.
 */
public class ServerEntityFinder {
    public static LivingEntity getEntityByUUID(ServerWorld world, UUID uuid) {
        for (Entity entity : world.iterateEntities()) {
            if (entity.getUuid().equals(uuid) && entity instanceof LivingEntity) {
                return (LivingEntity) entity;
            }
        }
        return null; // Entity not found
    }
}