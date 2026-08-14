package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;

public class Particles extends Module {

    private final BooleanSetting onHit = new BooleanSetting("OnHit", "Particles on attack", true);
    private final NumberSetting amount = new NumberSetting("Amount", "Particle count", 8, 1, 30, 1);

    public Particles() {
        super("Particles", "Красивые частицы при атаке", Category.RENDER);
        addSetting(onHit);
        addSetting(amount);
    }

    public static void spawnAttackParticles() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Module mod = null;
        if (com.appetir.modules.ModuleManager.getInstance() != null) {
            mod = com.appetir.modules.ModuleManager.getInstance().getByName("Particles");
        }
        if (mod == null || !mod.isEnabled()) return;
        if (!(mod instanceof Particles)) return;
        Particles p = (Particles) mod;
        if (!p.onHit.get()) return;

        int n = p.amount.getInt();
        for (int i = 0; i < n; i++) {
            double ox = (Math.random() - 0.5) * 0.6;
            double oy = (Math.random() - 0.5) * 0.6;
            double oz = (Math.random() - 0.5) * 0.6;
            mc.world.addParticle(
                    ParticleTypes.CRIT,
                    mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ(),
                    ox, oy, oz
            );
        }
    }
}
