package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.NumberSetting;
import com.appetir.util.Targeting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class AutoSwap extends Module {

    private final NumberSetting range = new NumberSetting("Range", "Enemy detect range", 5, 2, 12, 0.5);
    private int cooldown = 0;

    public AutoSwap() {
        super("AutoSwap", "Авто смена на меч/топор рядом с врагом", Category.COMBAT);
        addSetting(range);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (cooldown > 0) { cooldown--; return; }

        ItemStack held = mc.player.getMainHandStack();
        if (held.getItem() instanceof SwordItem || held.getItem() instanceof AxeItem) return;

        double rangeSq = range.get() * range.get();
        boolean enemyNear = false;
        for (var e : mc.world.getEntities()) {
            if (mc.player.squaredDistanceTo(e) >= rangeSq) continue;
            // #29: real enemies only (no friends / cows)
            if (Targeting.isDefaultEnemy(e)) {
                enemyNear = true;
                break;
            }
        }
        if (!enemyNear) return;

        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.inventory.getStack(i);
            if (s.getItem() instanceof SwordItem || s.getItem() instanceof AxeItem) {
                mc.player.inventory.selectedSlot = i;
                cooldown = 5;
                return;
            }
        }
    }
}
