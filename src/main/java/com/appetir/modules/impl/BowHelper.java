package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.Hand;

/**
 * Aims and charges bow via interactionManager — does not own keyUse.
 */
public class BowHelper extends Module {

    private int chargeTicks = 0;
    private boolean startedUse;

    public BowHelper() {
        super("BowHelper", "Помогает при стрельбе из лука", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        release();
        chargeTicks = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            release();
            return;
        }

        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) {
            release();
            chargeTicks = 0;
            return;
        }

        Entity target = null;
        double closest = 40 * 40;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity)) continue;
            if (e instanceof PlayerEntity && e.isSpectator()) continue;
            double d = mc.player.squaredDistanceTo(e);
            if (d < closest) {
                closest = d;
                target = e;
            }
        }

        if (target == null) {
            release();
            chargeTicks = 0;
            return;
        }

        double dx = target.getX() - mc.player.getX();
        double dy = target.getEyeY() - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        mc.player.yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        mc.player.pitch = (float) Math.toDegrees(-Math.atan((dy - dist * 0.1) / Math.max(dist, 0.001)));

        if (!startedUse && !mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            startedUse = true;
            chargeTicks = 0;
        }

        if (startedUse) {
            if (++chargeTicks >= 20) {
                // Release shot
                if (mc.player.isUsingItem()) {
                    mc.interactionManager.stopUsingItem(mc.player);
                }
                startedUse = false;
                chargeTicks = 0;
            }
        }
    }

    private void release() {
        if (!startedUse) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.interactionManager != null && mc.player.isUsingItem()) {
            mc.interactionManager.stopUsingItem(mc.player);
        }
        startedUse = false;
    }
}
