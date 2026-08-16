package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;

public class BowHelper extends Module {

    private int chargeTicks = 0;
    private boolean holdingUse;

    public BowHelper() {
        super("BowHelper", "Помогает при стрельбе из лука", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        releaseUse();
        chargeTicks = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options == null) {
            releaseUse();
            return;
        }

        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) {
            releaseUse();
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
            releaseUse();
            chargeTicks = 0;
            return;
        }

        double dx = target.getX() - mc.player.getX();
        double dy = target.getEyeY() - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        mc.player.yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        mc.player.pitch = (float) Math.toDegrees(-Math.atan((dy - dist * 0.1) / Math.max(dist, 0.001)));

        // Bow uses Use (RMB), not Attack
        mc.options.keyUse.setPressed(true);
        holdingUse = true;

        if (++chargeTicks >= 20) {
            releaseUse();
            chargeTicks = 0;
        }
    }

    private void releaseUse() {
        if (!holdingUse) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) mc.options.keyUse.setPressed(false);
        holdingUse = false;
    }
}
