package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

/**
 * Anti-AFK: Jump / Swing / Rotate only — no sneak state ownership (#30).
 */
public class AntiAFK extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Action", "Jump", "Jump", "Swing", "Rotate");
    private final NumberSetting interval = new NumberSetting("Interval", "Ticks between actions", 200, 40, 600, 20);
    private final BooleanSetting onlyGround = new BooleanSetting("OnlyGround", "Only act on ground", true);

    private int tick = 0;

    public AntiAFK() {
        super("AntiAFK", "Не даёт кикнуть за AFK", Category.MISC);
        addSetting(mode);
        addSetting(interval);
        addSetting(onlyGround);
    }

    @Override
    public void onDisable() {
        tick = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        tick++;
        if (tick < interval.getInt()) return;

        if (onlyGround.get() && !mc.player.isOnGround()) return;

        tick = 0;

        switch (mode.get()) {
            case "Jump":
                if (mc.player.isOnGround()) mc.player.jump();
                break;
            case "Swing":
                if (!mc.player.isUsingItem()) {
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
                break;
            case "Rotate":
                mc.player.yaw += 15;
                break;
            default:
                break;
        }
    }
}
