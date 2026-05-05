package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

// AirStuck останавливает игрока в воздухе блокируя движение и пакеты.
// Полная блокировка пакетов реализуется через mixin на ClientPlayNetworkHandler.
// Здесь управляем состоянием и обнуляем скорость каждый тик.
public class AirStuck extends Module {

    public AirStuck() {
        super("AirStuck", "Останавливает игрока в воздухе, блокируя пакеты", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        // Обнуляем скорость — игрок висит на месте
        mc.player.setVelocity(0, 0, 0);
        mc.player.noClip = true;
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.noClip = false;
    }
}
