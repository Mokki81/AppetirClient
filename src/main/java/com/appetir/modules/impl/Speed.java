package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributes;

public class Speed extends Module {
    private double savedSpeed = 0.1;
    public Speed() { super("Speed","Увеличивает скорость передвижения",Category.MOVEMENT); }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null) return;
        var attr = mc.player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (attr!=null) { savedSpeed=attr.getBaseValue(); attr.setBaseValue(savedSpeed*2.5); }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null) return;
        var attr = mc.player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (attr!=null) attr.setBaseValue(savedSpeed);
    }
}
