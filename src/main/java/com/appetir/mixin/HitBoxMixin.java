package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.HitBox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public class HitBoxMixin {

    @Inject(
            method = "raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void appetir$expandedRaycast(
            Entity entity, Vec3d min, Vec3d max, Box box,
            Predicate<Entity> predicate, double maxDistance,
            CallbackInfoReturnable<EntityHitResult> cir) {

        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        HitBox hitBox = null;
        for (com.appetir.modules.Module m : mm.getModules()) {
            if (m instanceof HitBox && m.isEnabled()) {
                hitBox = (HitBox) m;
                break;
            }
        }
        if (hitBox == null) return;

        float exp = HitBox.expansion;
        if (exp <= 0f) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        // #28: compare squared distances
        double best = maxDistance * maxDistance;
        Entity bestEntity = null;
        Vec3d bestHit = null;

        Box search = box.expand(exp);
        for (Entity e : mc.world.getOtherEntities(entity, search, predicate)) {
            if (!(e instanceof LivingEntity)) continue;
            if (mc.player != null && e == mc.player) continue;

            Box eb = e.getBoundingBox().expand(exp + e.getTargetingMargin());
            Optional<Vec3d> opt = eb.raycast(min, max);
            if (!opt.isPresent()) continue;

            Vec3d hit = opt.get();
            double d = min.squaredDistanceTo(hit);
            if (d < best) {
                best = d;
                bestEntity = e;
                bestHit = hit;
            }
        }

        if (bestEntity != null) {
            cir.setReturnValue(new EntityHitResult(bestEntity, bestHit));
        }
    }
}
