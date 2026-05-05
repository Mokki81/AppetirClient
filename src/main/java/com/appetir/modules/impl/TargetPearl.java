package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
        if (mc.player == null || mc.world == null) return;

        // Ищем жемчуг в хотбаре
        int pearlSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                pearlSlot = i; break;
            }
        }
        if (pearlSlot == -1) return;

        // Ближайший враг
        Entity target = null;
        double closest = range * range;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity)) continue;
            if (e.isSpectator()) continue;
            double d = mc.player.squaredDistanceTo(e);
            if (d < closest) { closest = d; target = e; }
        }
        if (target == null) return;

        // Наводимся и бросаем жемчуг
        double dx = target.getX() - mc.player.getX();
        double dy = target.getEyeY() - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        float yaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan((dy) / Math.sqrt(dx*dx+dz*dz)));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
        mc.player.getInventory().selectedSlot = pearlSlot;
        mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);

        // Выключаем после броска
        setEnabled(false);
    }
}
