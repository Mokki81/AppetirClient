package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

public class Sprint extends Module {
    public Sprint() { super("Sprint","Автоматический спринт",Category.MOVEMENT); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null) return;
        if (mc.player.isOnGround() && mc.options.keyForward.isPressed())
            mc.player.setSprinting(true);
    }
}
