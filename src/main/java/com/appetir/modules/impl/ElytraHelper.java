package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

/**
 * Deploys elytra via ClientCommand packet — does not own keyJump.
 */
public class ElytraHelper extends Module {

    private int cooldown;

    public ElytraHelper() {
        super("ElytraHelper", "Помощник для элитры", Category.MISC);
    }

    @Override
    public void onDisable() {
        cooldown = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (cooldown > 0) { cooldown--; return; }

        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) return;
        if (mc.player.isOnGround() || mc.player.isFallFlying()) return;
        if (mc.player.getVelocity().y >= -0.4) return;

        // Start fall-flying without touching keyJump
        mc.getNetworkHandler().sendPacket(
                new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        cooldown = 10;
    }
}
