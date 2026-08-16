package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

/**
 * Anti-AFK without owning vanilla key states (no keySneak hijack).
 */
public class AntiAFK extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Action", "Jump", "Jump", "Sneak", "Swing", "Rotate");
    private final NumberSetting interval = new NumberSetting("Interval", "Ticks between actions", 200, 40, 600, 20);
    private final BooleanSetting onlyGround = new BooleanSetting("OnlyGround", "Only act on ground", true);

    private int tick = 0;
    private boolean sneakPulse;
    private int sneakPulseTicks;

    public AntiAFK() {
        super("AntiAFK", "Не даёт кикнуть за AFK", Category.MISC);
        addSetting(mode);
        addSetting(interval);
        addSetting(onlyGround);
    }

    @Override
    public void onDisable() {
        sneakPulse = false;
        sneakPulseTicks = 0;
        // Do NOT touch keySneak — user may still be holding Shift
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Always advance interval clock
        tick++;

        // Short sneak pulse via player.setSneaking — does not own keySneak
        if (sneakPulse) {
            sneakPulseTicks--;
            mc.player.setSneaking(true);
            if (sneakPulseTicks <= 0) {
                sneakPulse = false;
                mc.player.setSneaking(false);
            }
            return;
        }

        if (tick < interval.getInt()) return;

        if (onlyGround.get() && !mc.player.isOnGround()) {
            // Wait until grounded — do not reset tick
            return;
        }

        tick = 0;

        switch (mode.get()) {
            case "Jump":
                if (mc.player.isOnGround()) mc.player.jump();
                break;
            case "Sneak":
                sneakPulse = true;
                sneakPulseTicks = 4; // ~0.2s pulse
                mc.player.setSneaking(true);
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
