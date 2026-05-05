package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class NightVision extends Module {
    public NightVision() { super("NightVision","Ночное зрение",Category.RENDER); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null) return;
        StatusEffectInstance e = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (e==null||e.getDuration()<40)
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,800,0,false,false));
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player!=null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
