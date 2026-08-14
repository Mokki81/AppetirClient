package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

public class AntiAFK extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Action", "Jump", "Jump", "Sneak", "Swing", "Rotate");
    private final NumberSetting interval = new NumberSetting("Interval", "Ticks between actions", 200, 40, 600, 20);
    private final BooleanSetting onlyGround = new BooleanSetting("OnlyGround", "Only when on ground", true);

    private int tick = 0;
    private boolean sneakToggle = false;

    public AntiAFK() {
        super("AntiAFK", "Не даёт кикнуть за AFK", Category.MISC);
        addSetting(mode);
        addSetting(interval);
        addSetting(onlyGround);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (onlyGround.get() && !mc.player.isOnGround()) return;

        if (++tick < interval.getInt()) return;
        tick = 0;

        switch (mode.get()) {
            case "Jump":
                if (mc.player.isOnGround()) mc.player.jump();
                break;
            case "Sneak":
                sneakToggle = !sneakToggle;
                mc.options.keySneak.setPressed(sneakToggle);
                break;
            case "Swing":
                mc.player.swingHand(mc.player.getActiveHand());
                break;
            case "Rotate":
                mc.player.yaw += 15;
                break;
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) mc.options.keySneak.setPressed(false);
    }
}
