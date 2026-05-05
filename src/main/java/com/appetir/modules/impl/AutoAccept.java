package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

// AutoAccept автоматически принимает /tpa запросы.
// Реализуется через mixin на ClientPlayNetworkHandler::onGameMessage.
public class AutoAccept extends Module {

    public static boolean enabled_flag = false;

    public AutoAccept() {
        super("AutoAccept", "Автоматически принимает телепорты", Category.MISC);
    }

    @Override public void onEnable()  { enabled_flag = true;  }
    @Override public void onDisable() { enabled_flag = false; }
}
