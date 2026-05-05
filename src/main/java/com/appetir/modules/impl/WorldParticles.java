package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;

public class WorldParticles extends Module {

    private int tick = 0;

    public WorldParticles() {
        super("WorldParticles", "Частицы в мире", Category.RENDER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (++tick % 5 != 0) return;

        double x = mc.player.getX() + (Math.random() - 0.5) * 8;
        double y = mc.player.getY() + Math.random() * 3;
        double z = mc.player.getZ() + (Math.random() - 0.5) * 8;

        mc.world.addParticle(ParticleTypes.END_ROD, x, y, z, 0,  0.02, 0);
    }
}
