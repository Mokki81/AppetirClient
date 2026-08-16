package com.appetir.util;

import com.appetir.friends.FriendManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Shared target policy for combat modules.
 */
public final class Targeting {

    private Targeting() {}

    public static boolean isFriend(Entity e) {
        if (!(e instanceof PlayerEntity)) return false;
        FriendManager fm = FriendManager.getInstance();
        return fm != null && fm.isFriendReadOnly((PlayerEntity) e);
    }

    public static boolean isValidCombatTarget(Entity e, boolean players, boolean hostiles, boolean animals) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || e == null || e == mc.player) return false;
        if (!(e instanceof LivingEntity)) return false;

        LivingEntity living = (LivingEntity) e;
        if (living.isDead() || living.getHealth() <= 0) return false;

        if (e instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity) e;
            if (p.isSpectator()) return false;
            if (isFriend(p)) return false;
            return players;
        }

        if (e instanceof HostileEntity) {
            return hostiles;
        }

        // passive / other living
        return animals;
    }

    /** Default combat: players + hostiles, never friends. */
    public static boolean isDefaultEnemy(Entity e) {
        return isValidCombatTarget(e, true, true, false);
    }
}
