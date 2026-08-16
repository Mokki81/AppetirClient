package com.appetir.modules.impl;

import com.appetir.client.ClientMode;
import com.appetir.friends.FriendManager;
import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

public class KillAura extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Legit or Rage", "Legit", "Legit", "Rage");
    private final NumberSetting range = new NumberSetting("Range", "Attack range", 3.2, 2.0, 6.0, 0.1);
    private final NumberSetting fov = new NumberSetting("FOV", "Max aim FOV (Legit)", 60, 20, 180, 5);
    private final NumberSetting minDelay = new NumberSetting("MinDelay", "Min ticks between hits", 2, 0, 20, 1);
    private final NumberSetting maxDelay = new NumberSetting("MaxDelay", "Max ticks between hits", 5, 0, 20, 1);
    private final NumberSetting aimSpeed = new NumberSetting("AimSpeed", "Soft aim speed", 0.45, 0.1, 1.0, 0.05);
    private final BooleanSetting players = new BooleanSetting("Players", "Attack players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", "Attack hostile mobs", false);
    private final BooleanSetting throughWalls = new BooleanSetting("ThroughWalls", "Ignore walls", false);
    private final BooleanSetting swing = new BooleanSetting("Swing", "Swing hand", true);
    private final BooleanSetting onlyCriticals = new BooleanSetting("OnlyCrits", "Only when falling (real crit)", false);
    private final ModeSetting priority = new ModeSetting("Priority", "Target priority", "Distance", "Distance", "Health");

    private int hitCooldown = 0;

    public KillAura() {
        super("KillAura", "Атакует ближайшего врага", Category.COMBAT);
        addSetting(mode);
        addSetting(range);
        addSetting(fov);
        addSetting(minDelay);
        addSetting(maxDelay);
        addSetting(aimSpeed);
        addSetting(players);
        addSetting(mobs);
        addSetting(throughWalls);
        addSetting(swing);
        addSetting(onlyCriticals);
        addSetting(priority);
    }

    @Override
    public void onTick() {
        if (ClientMode.isClean()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (hitCooldown > 0) {
            hitCooldown--;
            return;
        }

        boolean legit = mode.is("Legit");

        float cd = mc.player.getAttackCooldownProgress(0.5f);
        if (legit && cd < 0.9f) return;
        if (!legit && cd < 0.85f) return;

        if (onlyCriticals.get() && !canCritical(mc)) return;

        Entity target = findTarget(mc, legit);
        if (target == null) return;

        if (legit) {
            softLook(mc, target);
            if (angleTo(mc.player, target) > fov.get()) return;
        } else {
            hardLook(mc, target);
        }

        mc.interactionManager.attackEntity(mc.player, target);
        if (swing.get()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        int min = Math.min(minDelay.getInt(), maxDelay.getInt());
        int max = Math.max(minDelay.getInt(), maxDelay.getInt());
        hitCooldown = min + ThreadLocalRandom.current().nextInt(max - min + 1);
    }

    /**
     * Real critical-hit window: airborne, falling, not climbing/swimming/flying/blind.
     */
    private boolean canCritical(MinecraftClient mc) {
        PlayerEntity p = mc.player;
        if (p == null) return false;
        if (p.isOnGround()) return false;
        if (p.isTouchingWater() || p.isSubmergedInWater()) return false;
        if (p.isClimbing()) return false;
        if (p.getAbilities().flying) return false;
        if (p.hasVehicle()) return false;
        // Must be falling (negative Y velocity) — not rising after jump
        return p.getVelocity().y < -0.08;
    }

    private void softLook(MinecraftClient mc, Entity target) {
        Vec3d eyes = mc.player.getCameraPosVec(1.0f);
        Vec3d aim = target.getPos().add(0, target.getHeight() * 0.85, 0);
        Vec3d diff = aim.subtract(eyes);

        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float targetYaw = (float) (MathHelper.atan2(diff.z, diff.x) * (180.0 / Math.PI)) - 90.0f;
        float targetPitch = (float) (-(MathHelper.atan2(diff.y, dist) * (180.0 / Math.PI)));

        float speed = aimSpeed.getFloat();
        float noise = (ThreadLocalRandom.current().nextFloat() - 0.5f) * 1.2f;

        mc.player.yaw = lerpAngle(mc.player.yaw, targetYaw + noise, speed);
        mc.player.pitch = MathHelper.clamp(
                lerpAngle(mc.player.pitch, targetPitch + noise * 0.3f, speed),
                -90f, 90f);
    }

    private void hardLook(MinecraftClient mc, Entity target) {
        Vec3d eyes = mc.player.getCameraPosVec(1.0f);
        Vec3d aim = target.getPos().add(0, target.getHeight() * 0.85, 0);
        Vec3d diff = aim.subtract(eyes);
        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        mc.player.yaw = (float) (MathHelper.atan2(diff.z, diff.x) * (180.0 / Math.PI)) - 90.0f;
        mc.player.pitch = (float) (-(MathHelper.atan2(diff.y, dist) * (180.0 / Math.PI)));
    }

    private static float lerpAngle(float from, float to, float t) {
        float d = MathHelper.wrapDegrees(to - from);
        return from + d * t;
    }

    private static double angleTo(PlayerEntity player, Entity target) {
        Vec3d eyes = player.getCameraPosVec(1.0f);
        Vec3d aim = target.getPos().add(0, target.getHeight() * 0.85, 0);
        Vec3d diff = aim.subtract(eyes).normalize();
        Vec3d look = player.getRotationVec(1.0f).normalize();
        double dot = MathHelper.clamp(look.dotProduct(diff), -1.0, 1.0);
        return Math.toDegrees(Math.acos(dot));
    }

    private Entity findTarget(MinecraftClient mc, boolean legit) {
        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        double maxRange = legit ? Math.min(range.get(), 3.5) : range.get();
        double maxRangeSq = maxRange * maxRange;
        double maxFov = legit ? fov.get() : 360;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity) e;
            if (living.isDead() || living.getHealth() <= 0) continue;

            if (e instanceof PlayerEntity) {
                if (!players.get()) continue;
                if (e.isSpectator()) continue;
                if (FriendManager.getInstance() != null && FriendManager.getInstance().isFriend((PlayerEntity) e))
                    continue;
            } else if (e instanceof HostileEntity) {
                if (!mobs.get()) continue;
            } else {
                continue;
            }

            double distSq = mc.player.squaredDistanceTo(e);
            if (distSq > maxRangeSq) continue;
            if (!throughWalls.get() && !mc.player.canSee(e)) continue;
            if (angleTo(mc.player, e) > maxFov) continue;

            double score = priority.is("Health") ? living.getHealth() : distSq;
            if (score < bestScore) {
                bestScore = score;
                best = e;
            }
        }
        return best;
    }
}
