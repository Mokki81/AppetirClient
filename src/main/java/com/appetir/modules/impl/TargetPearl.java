package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.util.Targeting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class TargetPearl extends Module {

    private final float range = 20.0f;

    public TargetPearl() {
        super("TargetPearl", "Пёрл в цель", Category.MISC);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        int pearlSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStack(i).getItem() == Items.ENDER_PEARL) {
                pearlSlot = i;
                break;
            }
        }
        if (pearlSlot == -1) {
            setEnabled(false);
            return;
        }

        Entity target = null;
        double closest = range * range;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity)) continue;
            if (!Targeting.isDefaultEnemy(e)) continue;
            double d = mc.player.squaredDistanceTo(e);
            if (d < closest) {
                closest = d;
                target = e;
            }
        }
        if (target == null) {
            setEnabled(false);
            return;
        }

        double dx = target.getX() - mc.player.getX();
        double dy = target.getEyeY() - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan(dy / Math.sqrt(dx * dx + dz * dz)));

        int prev = mc.player.inventory.selectedSlot;
        mc.player.yaw = yaw;
        mc.player.pitch = pitch;
        mc.player.inventory.selectedSlot = pearlSlot;
        mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
        mc.player.inventory.selectedSlot = prev;

        setEnabled(false);
    }
}
