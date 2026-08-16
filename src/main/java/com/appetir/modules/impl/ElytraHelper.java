package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;

public class ElytraHelper extends Module {

    private int jumpHoldTicks = 0;

    public ElytraHelper() {
        super("ElytraHelper", "Помощник для элитры", Category.MISC);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null && jumpHoldTicks > 0) {
            mc.options.keyJump.setPressed(false);
        }
        jumpHoldTicks = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null) return;

        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            if (jumpHoldTicks > 0) {
                mc.options.keyJump.setPressed(false);
                jumpHoldTicks = 0;
            }
            return;
        }

        if (mc.player.isOnGround() || mc.player.isFallFlying()) {
            if (jumpHoldTicks > 0) {
                mc.options.keyJump.setPressed(false);
                jumpHoldTicks = 0;
            }
            return;
        }

        // Hold jump for a few ticks while falling to deploy elytra
        if (mc.player.getVelocity().y < -0.4) {
            mc.options.keyJump.setPressed(true);
            jumpHoldTicks = 3;
        } else if (jumpHoldTicks > 0) {
            jumpHoldTicks--;
            if (jumpHoldTicks == 0) {
                mc.options.keyJump.setPressed(false);
            }
        }
    }
}
