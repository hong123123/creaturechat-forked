package com.owlmaddie.mixin;

import com.owlmaddie.chat.ChatDataManager;
import com.owlmaddie.chat.EntityChatData;
import com.owlmaddie.chat.PlayerData;
import com.owlmaddie.network.ServerPackets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The {@code MixinLivingEntity} class modifies the behavior of {@link LivingEntity} to integrate
 * custom friendship, chat, and death message mechanics. It prevents friendly entities from targeting players,
 * generates contextual chat messages on attacks, and broadcasts custom death messages for named entities.
 */
@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    private EntityChatData getChatData(LivingEntity entity) {
        ChatDataManager chatDataManager = ChatDataManager.getServerInstance();
        return chatDataManager.getOrCreateChatData(entity.getUuidAsString());
    }

    @Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void modifyCanTarget(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof PlayerEntity) {
            LivingEntity thisEntity = (LivingEntity) (Object) this;
            EntityChatData entityData = getChatData(thisEntity);
            PlayerData playerData = entityData.getPlayerData(target.getDisplayName().getString());
            if (playerData.friendship > 0) {
                // Friendly creatures can't target a player
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "damage", at = @At(value = "RETURN"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            // If damage method returned false, it means the damage was not applied (possibly due to invulnerability).
            return;
        }

        // Get attacker and entity objects
        Entity attacker = source.getAttacker();
        LivingEntity thisEntity = (LivingEntity) (Object) this;

        // If PLAYER attacks MOB then
        if (attacker instanceof PlayerEntity && thisEntity instanceof MobEntity && !thisEntity.isDead()) {
            // Generate attacked message (only if the previous user message was not an attacked message)
            // We don't want to constantly generate messages during a prolonged, multi-damage event
            ServerPlayerEntity player = (ServerPlayerEntity) attacker;
            EntityChatData chatData = getChatData(thisEntity);
            if (!chatData.characterSheet.isEmpty() && chatData.auto_generated < ChatDataManager.MAX_AUTOGENERATE_RESPONSES) {
                // Only auto-generate a response to being attacked if chat data already exists
                // and this is the first attack event.
                ItemStack weapon = player.getMainHandStack();
                String weaponName = weapon.isEmpty() ? "with fists" : "with " + weapon.getItem().toString();

                // Determine if the damage was indirect
                boolean isIndirect = source.isIndirect();
                String directness = isIndirect ? "indirectly" : "directly";

                String attackedMessage = "<" + player.getName().getString() + " attacked you " + directness + " with " + weaponName + ">";
                ServerPackets.generate_chat("N/A", chatData, player, (MobEntity) thisEntity, attackedMessage, true);
            }
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo info) {
        LivingEntity entity = (LivingEntity) (Object) this;
        World world = entity.getWorld();

        if (!world.isClient() && entity.hasCustomName()) {
            // Skip tamed entities and players
            if (entity instanceof TameableEntity && ((TameableEntity) entity).isTamed()) {
                return;
            }

            if (entity instanceof PlayerEntity) {
                return;
            }

            // Get chatData for the entity
            EntityChatData chatData = getChatData(entity);
            if (chatData != null && !chatData.characterSheet.isEmpty()) {
                // Get the original death message
                Text deathMessage = entity.getDamageTracker().getDeathMessage();
                // Broadcast the death message to all players in the world
                ServerPackets.BroadcastMessage(deathMessage);
            }
        }
    }
}
