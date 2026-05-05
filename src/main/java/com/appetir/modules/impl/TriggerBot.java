package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
        if (mc.player == null || mc.world == null) return;
        if (cooldown > 0) { cooldown--; return; }
        if (mc.player.getAttackCooldownProgress(0) < 1.0f) return;

        // Проверяем на что смотрит игрок
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult entityHit = (EntityHitResult) hit;
        if (!(entityHit.getEntity() instanceof LivingEntity)) return;
        if (entityHit.getEntity() instanceof PlayerEntity &&
            entityHit.getEntity().isSpectator()) return;

        LivingEntity target = (LivingEntity) entityHit.getEntity();
        if (target.isDead()) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        cooldown = 5; // небольшая задержка между атаками
    }
}
