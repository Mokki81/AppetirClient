package com.appetir.modules.impl;
import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
public class AntiAFK extends Module {
    private int tick = 0;
    public AntiAFK() { super("AntiAFK", "Прыгает чтобы не кикнули", Category.MISC); }
    @Override public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (++tick % 200 == 0) mc.player.jump();
    }
}
