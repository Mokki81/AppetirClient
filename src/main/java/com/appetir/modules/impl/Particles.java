package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleTypes;

public class Particles extends Module {

    public Particles() {
        super("Particles", "Частицы при атаке и броске", Category.RENDER);
    }

    // Вызывается из mixin при атаке игрока
    public static void spawnAttackParticles() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        for (int i = 0; i < 8; i++) {
            double ox = (Math.random() - 0.5) * 0.5;
            double oy = (Math.random() - 0.5) * 0.5;
            double oz = (Math.random() - 0.5) * 0.5;
            mc.world.addParticle(
                ParticleTypes.CRIT,
                mc.player.getX(), mc.player.getY() + 1, mc.player.getZ(),
                ox, oy, oz
            );
        }
    }
}
