package com.owlmaddie.particle;

import net.minecraft.entity.Entity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

import static com.owlmaddie.network.ServerPackets.*;

/**
 * The {@code ParticleEmitter} class provides utility methods for emitting custom particles and sounds
 * around entities in the game. It calculates particle positions based on entity orientation
 * and triggers sound effects based on particle type and count.
 */
public class ParticleEmitter {
    public static void emitCreatureParticle(ServerWorld world, Entity entity, DefaultParticleType particleType, double spawnSize, int count) {
        // Calculate the offset for the particle to appear above and in front of the entity
        float yaw = entity.getHeadYaw();
        double offsetX = -MathHelper.sin(yaw * ((float) Math.PI / 180F)) * 0.9;
        double offsetY = entity.getHeight() + 0.5;
        double offsetZ = MathHelper.cos(yaw * ((float) Math.PI / 180F)) * 0.9;

        // Final position
        double x = entity.getX() + offsetX;
        double y = entity.getY() + offsetY;
        double z = entity.getZ() + offsetZ;

        // Emit the custom particle on the server
        world.spawnParticles(particleType, x, y, z, count, spawnSize, spawnSize, spawnSize, 0.1F);

        // Play sound when lots of hearts are emitted
        if (particleType.equals(HEART_BIG_PARTICLE) && count > 1) {
            world.playSound(entity, entity.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.4F, 1.0F);
        } else if (particleType.equals(FIRE_BIG_PARTICLE) && count > 1) {
            world.playSound(entity, entity.getBlockPos(), SoundEvents.ITEM_AXE_STRIP, SoundCategory.PLAYERS, 0.8F, 1.0F);
        } else if (particleType.equals(FOLLOW_FRIEND_PARTICLE) || particleType.equals(FOLLOW_ENEMY_PARTICLE) ||
                particleType.equals(LEAD_FRIEND_PARTICLE) || particleType.equals(LEAD_ENEMY_PARTICLE)) {
            world.playSound(entity, entity.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_PLACE, SoundCategory.PLAYERS, 0.8F, 1.0F);
        } else if (particleType.equals(PROTECT_PARTICLE)) {
            world.playSound(entity, entity.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.8F, 1.0F);
        }
    }
}