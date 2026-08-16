package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.util.Targeting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TriggerBot extends Module {

    private int cooldown = 0;

    public TriggerBot() {
        super("TriggerBot", "Атакует автоматически когда прицел на враге", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (cooldown > 0) { cooldown--; return; }
        if (mc.player.getAttackCooldownProgress(0) < 1.0f) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) hit).getEntity();
        if (!Targeting.isDefaultEnemy(entity)) return;

        mc.interactionManager.attackEntity(mc.player, entity);
        mc.player.swingHand(Hand.MAIN_HAND);
        cooldown = 5;
    }
}
