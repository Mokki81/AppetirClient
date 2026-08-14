package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class NightVision extends Module {

    private final BooleanSetting hideParticles = new BooleanSetting("HideParticles", "No effect particles", true);

    public NightVision() {
        super("NightVision", "Ночное зрение", Category.RENDER);
        addSetting(hideParticles);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        StatusEffectInstance e = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (e == null || e.getDuration() < 220) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, 400, 0, false, !hideParticles.get()));
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
