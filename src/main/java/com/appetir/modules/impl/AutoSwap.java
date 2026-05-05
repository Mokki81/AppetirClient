package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class AutoSwap extends Module {
    public AutoSwap() { super("AutoSwap","По кнопке меняет предметы в руках",Category.COMBAT); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null||mc.world==null) return;
        ItemStack held = mc.player.getMainHandStack();
        if (held.getItem() instanceof SwordItem||held.getItem() instanceof AxeItem) return;
        boolean enemyNear = mc.world.getEntities().stream()
            .filter(e->e!=mc.player).anyMatch(e->mc.player.squaredDistanceTo(e)<25.0);
        if (!enemyNear) return;
        for (int i=0;i<9;i++) {
            ItemStack s = mc.player.inventory.getStack(i);
            if (s.getItem() instanceof SwordItem||s.getItem() instanceof AxeItem) {
                mc.player.inventory.selectedSlot=i; return;
            }
        }
    }
}
