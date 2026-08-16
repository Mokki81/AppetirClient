package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class BowHelper extends Module {

    private final BooleanSetting playersOnly = new BooleanSetting("PlayersOnly", "Only aim players", true);
    private final BooleanSetting requireLos = new BooleanSetting("LineOfSight", "Require clear LOS", true);
    private final BooleanSetting softAim = new BooleanSetting("SoftAim", "Smooth aim (don't snap)", true);
    private final NumberSetting range = new NumberSetting("Range", "Max target range", 40, 10, 64, 2);
    private final NumberSetting aimSpeed = new NumberSetting("AimSpeed", "Soft aim factor", 0.35, 0.1, 1.0, 0.05);

    private int chargeTicks = 0;
    private boolean startedUse;

    public BowHelper() {
        super("BowHelper", "Помогает при стрельбе из лука", Category.COMBAT);
        addSetting(playersOnly);
        addSetting(requireLos);
        addSetting(softAim);
        addSetting(range);
        addSetting(aimSpeed);
    }

    @Override
    public void onDisable() {
        release();
        chargeTicks = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            release();
            return;
        }

        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) {
            release();
            chargeTicks = 0;
            return;
        }

        if (!hasArrows(mc)) {
            release();
            chargeTicks = 0;
            return;
        }

        Entity target = findTarget(mc);
        if (target == null) {
            release();
            chargeTicks = 0;
            return;
        }

        aimAt(mc, target);

        if (!startedUse && !mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            startedUse = true;
            chargeTicks = 0;
        }

        if (startedUse) {
            // Prefer vanilla pull progress when possible
            float pull = BowItem.getPullProgress(mc.player.getItemUseTime());
            boolean full = pull >= 0.95f || ++chargeTicks >= 22;
            if (full) {
                if (mc.player.isUsingItem()) {
                    mc.interactionManager.stopUsingItem(mc.player);
                }
                startedUse = false;
                chargeTicks = 0;
            }
        }
    }

    private Entity findTarget(MinecraftClient mc) {
        Entity best = null;
        double closest = range.get() * range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity) e;
            if (living.isDead() || living.getHealth() <= 0) continue;

            if (playersOnly.get()) {
                if (!(e instanceof PlayerEntity)) continue;
                if (((PlayerEntity) e).isSpectator()) continue;
            } else {
                // players + hostiles
                if (!(e instanceof PlayerEntity) && !(e instanceof HostileEntity)) continue;
                if (e instanceof PlayerEntity && ((PlayerEntity) e).isSpectator()) continue;
            }

            double d = mc.player.squaredDistanceTo(e);
            if (d >= closest) continue;

            if (requireLos.get() && !hasLineOfSight(mc, e)) continue;

            closest = d;
            best = e;
        }
        return best;
    }

    private boolean hasLineOfSight(MinecraftClient mc, Entity target) {
        Vec3d from = mc.player.getCameraPosVec(1.0f);
        Vec3d to = target.getBoundingBox().getCenter();
        HitResult hit = mc.world.raycast(new RaycastContext(
                from, to,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player));
        if (hit.getType() == HitResult.Type.MISS) return true;
        // Hit something before target — blocked
        return hit.getPos().squaredDistanceTo(to) < 1.0;
    }

    private void aimAt(MinecraftClient mc, Entity target) {
        double dx = target.getX() - mc.player.getX();
        double dy = target.getEyeY() - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) Math.toDegrees(-Math.atan((dy - dist * 0.1) / Math.max(dist, 0.001)));

        if (softAim.get()) {
            float speed = aimSpeed.getFloat();
            mc.player.yaw = lerpAngle(mc.player.yaw, targetYaw, speed);
            mc.player.pitch = mc.player.pitch + (targetPitch - mc.player.pitch) * speed;
        } else {
            mc.player.yaw = targetYaw;
            mc.player.pitch = targetPitch;
        }
    }

    private static float lerpAngle(float from, float to, float t) {
        float diff = to - from;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;
        return from + diff * t;
    }

    private boolean hasArrows(MinecraftClient mc) {
        if (mc.player.abilities.creativeMode) return true;
        ItemStack main = mc.player.getMainHandStack();
        if (main.getItem() == Items.BOW && main.hasEnchantments()) {
            // Infinity check via NBT is version-specific; scan inventory for arrows too
        }
        for (int i = 0; i < mc.player.inventory.size(); i++) {
            ItemStack s = mc.player.inventory.getStack(i);
            if (s.getItem() instanceof ArrowItem) return true;
            if (s.getItem() == Items.ARROW || s.getItem() == Items.SPECTRAL_ARROW
                    || s.getItem() == Items.TIPPED_ARROW) return true;
        }
        // Infinity enchantment on bow
        return net.minecraft.enchantment.EnchantmentHelper.getLevel(
                net.minecraft.enchantment.Enchantments.INFINITY, main) > 0;
    }

    private void release() {
        if (!startedUse) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.interactionManager != null && mc.player.isUsingItem()) {
            mc.interactionManager.stopUsingItem(mc.player);
        }
        startedUse = false;
    }
}
