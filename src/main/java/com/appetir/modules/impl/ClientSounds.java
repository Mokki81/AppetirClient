package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class ClientSounds extends Module {

    public ClientSounds() {
        super("ClientSounds", "Звуки", Category.MISC);
    }

    // Вызывается из mixin при включении/выключении модуля
    public static void playToggleOn() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        mc.world.playSound(mc.player, mc.player.getBlockPos(),
            SoundEvents.UI_BUTTON_CLICK, net.minecraft.sound.SoundCategory.MASTER,
            0.3f, 1.2f);
    }

    public static void playToggleOff() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        mc.world.playSound(mc.player, mc.player.getBlockPos(),
            SoundEvents.UI_BUTTON_CLICK, net.minecraft.sound.SoundCategory.MASTER,
            0.3f, 0.8f);
    }
}
