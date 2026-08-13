package com.appetir.modules.impl;

import com.appetir.friends.FriendManager;
import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class KillAura extends Module {

    private final NumberSetting range = new NumberSetting("Range", "Attack range", 4.2, 2.0, 6.0, 0.1);
    private final BooleanSetting players = new BooleanSetting("Players", "Attack players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", "Attack hostile mobs", false);
    private final BooleanSetting throughWalls = new BooleanSetting("ThroughWalls", "Ignore walls", false);
    private final BooleanSetting swing = new BooleanSetting("Swing", "Swing hand", true);
    private final ModeSetting priority = new ModeSetting("Priority", "Target priority", "Distance", "Distance", "Health");

    public KillAura() {
        super("KillAura", "Атакует ближайшего врага", Category.COMBAT);
        addSetting(range);
        addSetting(players);
        addSetting(mobs);
        addSetting(throughWalls);
        addSetting(swing);
        addSetting(priority);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0) < 1.0f) return;

        Entity target = findTarget(mc);
        if (target == null) return;

        mc.player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos().add(0, target.getHeight() * 0.85, 0));
        mc.interactionManager.attackEntity(mc.player, target);
        if (swing.get()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private Entity findTarget(MinecraftClient mc) {
        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        double maxRangeSq = range.get() * range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity) e;
            if (living.isDead() || living.getHealth() <= 0) continue;

            if (e instanceof PlayerEntity) {
                if (!players.get()) continue;
                if (e.isSpectator()) continue;
                if (FriendManager.getInstance() != null && FriendManager.getInstance().isFriend((PlayerEntity) e)) continue;
            } else if (e instanceof HostileEntity) {
                if (!mobs.get()) continue;
            } else {
                continue;
            }

            double distSq = mc.player.squaredDistanceTo(e);
            if (distSq > maxRangeSq) continue;

            if (!throughWalls.get() && !mc.player.canSee(e)) continue;

            double score;
            if (priority.is("Health")) {
                score = living.getHealth();
            } else {
                score = distSq;
            }

            if (score < bestScore) {
                bestScore = score;
                best = e;
            }
        }
        return best;
    }
}
