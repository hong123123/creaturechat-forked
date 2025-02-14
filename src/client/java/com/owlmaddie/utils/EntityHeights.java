package com.owlmaddie.utils;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.Entity;

/**
 * The {@code EntityHeightMap} class returns an adjusted entity height (which fixes certain MobEntity's with
 * unusually tall heights)
 */
public class EntityHeights {
    public static float getAdjustedEntityHeight(Entity entity) {
        // Get entity height (adjust for specific classes)
        float entityHeight = entity.getHeight();
        if (entity instanceof EnderDragonEntity) {
            entityHeight = 3F;
        }
        return entityHeight;
    }
}
