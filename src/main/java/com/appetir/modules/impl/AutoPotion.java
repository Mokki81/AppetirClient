package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoPotion extends Module {

    private static final float HEALTH_THRESHOLD = 10.0f; // половина хп
    private int cooldown = 0;

    public AutoPotion() {
        super("AutoPotion", "Автоматически использует зелья в нужный момент", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (cooldown > 0) { cooldown--; return; }
        if (mc.player.getHealth() > HEALTH_THRESHOLD) return;

        // Ищем зелье лечения или регенерации в хотбаре
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.POTION) {
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
                cooldown = 20;
                return;
            }
        }
    }
}
