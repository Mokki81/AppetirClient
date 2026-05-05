package com.appetir.mixin;

import com.appetir.modules.impl.AutoAccept;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class AutoAcceptMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!AutoAccept.enabled_flag) return;
        String msg = packet.getMessage().getString().toLowerCase();
        if (msg.contains("tpa") || msg.contains("teleport") || msg.contains("телепорт")) {
            MinecraftClient.getInstance().player.sendChatMessage("/tpaccept");
        }
    }
}
