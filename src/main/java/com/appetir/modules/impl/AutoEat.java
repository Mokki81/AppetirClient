package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;

public class AutoEat extends Module {
    private int cooldown=0;
    public AutoEat() { super("AutoEat","Автоматически ест еду",Category.MISC); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null) return;
        if (cooldown-->0) return;
        if (mc.player.getHungerManager().getFoodLevel()>=17) return;
        if (mc.player.isUsingItem()) return;

        for (int i=0;i<9;i++) {
            ItemStack s = mc.player.inventory.getStack(i);
            if (s.getItem().isFood()) {
                mc.player.inventory.selectedSlot=i;
                mc.options.keyUse.setPressed(true);
                cooldown=32; return;
            }
        }
        mc.options.keyUse.setPressed(false);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player!=null) mc.options.keyUse.setPressed(false);
    }
}
