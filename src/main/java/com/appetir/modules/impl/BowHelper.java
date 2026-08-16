package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
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
    private final BooleanSetting softAim = new BooleanSetting("SoftAim", "Smooth aim", true);
    private final BooleanSetting autoShoot = new BooleanSetting("AutoShoot", "Auto charge and release", true);
    private final NumberSetting range = new NumberSetting("Range", "Max target range", 40, 10, 64, 2);
    private final NumberSetting aimSpeed = new NumberSetting("AimSpeed", "Soft aim factor", 0.35, 0.1, 1.0, 0.05);
    private final NumberSetting shotCooldown = new NumberSetting("Cooldown", "Ticks after shot", 8, 0, 40, 1);

    private boolean moduleOwnsUse;
    private int postShotCooldown;

    public BowHelper() {
        super("BowHelper", "Помогает при стрельбе из лука", Category.COMBAT);
        addSetting(playersOnly);
        addSetting(requireLos);
        addSetting(softAim);
        addSetting(autoShoot);
        addSetting(range);
        addSetting(aimSpeed);
        addSetting(shotCooldown);
    }

    @Override
    public void onDisable() {
        releaseIfOurs();
        postShotCooldown = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            releaseIfOurs();
            return;
        }

        if (postShotCooldown > 0) {
            postShotCooldown--;
            return;
        }

        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) {
            releaseIfOurs();
            return;
        }

        if (!hasArrows(mc)) {
            releaseIfOurs();
            return;
        }

        Entity target = findTarget(mc);

        // User already using bow — only soft-aim assist, never release their use
        if (mc.player.isUsingItem() && !moduleOwnsUse) {
            if (target != null) aimAt(mc, target);
            return;
        }

        if (target == null) {
            releaseIfOurs();
            return;
        }

        aimAt(mc, target);

        if (!autoShoot.get()) return;

        if (!moduleOwnsUse) {
            if (mc.player.isUsingItem()) return; // user owns it
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            if (mc.player.isUsingItem()) {
                moduleOwnsUse = true;
            }
            return;
        }

        // We own the charge
        if (!mc.player.isUsingItem()) {
            // Use ended unexpectedly
            moduleOwnsUse = false;
            postShotCooldown = shotCooldown.getInt();
            return;
        }

        float pull = BowItem.getPullProgress(mc.player.getItemUseTime());
        if (pull >= 0.95f) {
            mc.interactionManager.stopUsingItem(mc.player);
            moduleOwnsUse = false;
            postShotCooldown = shotCooldown.getInt();
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

    /** Multi-point LOS: head / center / feet — any clear = visible. */
    private boolean hasLineOfSight(MinecraftClient mc, Entity target) {
        Vec3d from = mc.player.getCameraPosVec(1.0f);
        double x = target.getX();
        double z = target.getZ();
        double[] ys = {
                target.getY() + target.getHeight() * 0.9, // head
                target.getY() + target.getHeight() * 0.5, // center
                target.getY() + target.getHeight() * 0.15 // feet
        };
        for (double y : ys) {
            Vec3d to = new Vec3d(x, y, z);
            HitResult hit = mc.world.raycast(new RaycastContext(
                    from, to,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player));
            if (hit.getType() == HitResult.Type.MISS) return true;
            // Hit point near the sample → effectively clear to body
            if (hit.getPos().squaredDistanceTo(to) < 0.25) return true;
        }
        return false;
    }

    private void aimAt(MinecraftClient mc, Entity target) {
        double dx = target.getX() - mc.player.getX();
        double dy = (target.getY() + target.getHeight() * 0.5) - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        // Simple gravity-ish compensation: higher arc for longer range
        // bow projectile gravity ≈ 0.05, velocity ≈ 3 * pull; use mid charge estimate
        double v = 2.5;
        double g = 0.05;
        double pitchRad;
        if (dist < 0.1) {
            pitchRad = dy > 0 ? -Math.PI / 2 : Math.PI / 2;
        } else {
            // quadratic aim approximation
            double disc = v * v * v * v - g * (g * dist * dist + 2 * dy * v * v);
            if (disc < 0) {
                pitchRad = -Math.atan2(dy, dist);
            } else {
                pitchRad = Math.atan((v * v - Math.sqrt(disc)) / (g * dist));
            }
        }

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) -Math.toDegrees(pitchRad);

        if (softAim.get()) {
            float speed = aimSpeed.getFloat();
            float maxDelta = 12.0f; // max degrees per tick
            float yawDiff = wrapDegrees(targetYaw - mc.player.yaw);
            float pitchDiff = targetPitch - mc.player.pitch;
            yawDiff = clamp(yawDiff * speed, -maxDelta, maxDelta);
            pitchDiff = clamp(pitchDiff * speed, -maxDelta, maxDelta);
            mc.player.yaw += yawDiff;
            mc.player.pitch = clamp(mc.player.pitch + pitchDiff, -90, 90);
        } else {
            mc.player.yaw = targetYaw;
            mc.player.pitch = clamp(targetPitch, -90, 90);
        }
    }

    private static float wrapDegrees(float deg) {
        while (deg < -180) deg += 360;
        while (deg > 180) deg -= 360;
        return deg;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private boolean hasArrows(MinecraftClient mc) {
        if (mc.player.abilities.creativeMode) return true;
        ItemStack main = mc.player.getMainHandStack();
        if (EnchantmentHelper.getLevel(Enchantments.INFINITY, main) > 0) return true;
        for (int i = 0; i < mc.player.inventory.size(); i++) {
            ItemStack s = mc.player.inventory.getStack(i);
            if (s.getItem() == Items.ARROW
                    || s.getItem() == Items.SPECTRAL_ARROW
                    || s.getItem() == Items.TIPPED_ARROW) {
                return true;
            }
        }
        return false;
    }

    private void releaseIfOurs() {
        if (!moduleOwnsUse) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.interactionManager != null && mc.player.isUsingItem()) {
            mc.interactionManager.stopUsingItem(mc.player);
        }
        moduleOwnsUse = false;
    }
}
