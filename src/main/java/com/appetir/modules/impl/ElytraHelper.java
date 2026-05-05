package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;

public class ElytraHelper extends Module {
    public ElytraHelper() { super("ElytraHelper","Помощник для элитры",Category.MISC); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null) return;
        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem()!=Items.ELYTRA) return;
        if (mc.player.isOnGround()||mc.player.isFallFlying()) return;
        if (mc.player.getVelocity().getY()<-0.5) {
            mc.options.keyJump.setPressed(true);
            mc.options.keyJump.setPressed(false);
        }
    }
}
