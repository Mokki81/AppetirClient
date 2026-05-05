package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class KillAura extends Module {
    private final float range = 4.5f;
    public KillAura() { super("KillAura","Атакует ближайшего врага",Category.COMBAT); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null||mc.world==null) return;
        if (mc.player.getAttackCooldownProgress(0)<1.0f) return;

        Entity closest=null; double closest2=range*range;
        for (Entity e : mc.world.getEntities()) {
            if (e==mc.player||!(e instanceof LivingEntity)) continue;
            if (e instanceof PlayerEntity&&e.isSpectator()) continue;
            if (((LivingEntity)e).isDead()) continue;
            double d=mc.player.squaredDistanceTo(e);
            if (d<closest2) { closest2=d; closest=e; }
        }
        if (closest!=null) {
            mc.player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, closest.getPos());
            mc.interactionManager.attackEntity(mc.player, closest);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
