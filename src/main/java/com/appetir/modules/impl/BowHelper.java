package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;

public class BowHelper extends Module {
    private int chargeTicks=0;
    public BowHelper() { super("BowHelper","Помогает при стрельбе из лука",Category.COMBAT); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null||mc.world==null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) { chargeTicks=0; return; }

        Entity target=null; double closest=40*40;
        for (Entity e:mc.world.getEntities()) {
            if (e==mc.player||!(e instanceof LivingEntity)) continue;
            if (e instanceof PlayerEntity&&e.isSpectator()) continue;
            double d=mc.player.squaredDistanceTo(e);
            if (d<closest) { closest=d; target=e; }
        }
        if (target==null) return;

        double dx=target.getX()-mc.player.getX(), dy=target.getEyeY()-mc.player.getEyeY(), dz=target.getZ()-mc.player.getZ();
        double dist=Math.sqrt(dx*dx+dz*dz);
        mc.player.yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));
        mc.player.pitch=(float)Math.toDegrees(-Math.atan((dy-dist*0.1)/dist));

        mc.options.keyAttack.setPressed(true);
        if (++chargeTicks>=20) { mc.options.keyAttack.setPressed(false); chargeTicks=0; }
    }
}
