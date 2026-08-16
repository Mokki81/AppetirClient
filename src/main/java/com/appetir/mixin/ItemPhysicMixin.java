package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.ItemPhysic;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * OnlyFalling=true  → animate only in air; on ground leave vanilla pose.
 * OnlyFalling=false → also flatten items that are truly on the ground.
 */
@Mixin(ItemEntityRenderer.class)
public class ItemPhysicMixin {

    @Inject(
            method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void onRender(ItemEntity entity, float yaw, float tickDelta,
                          MatrixStack matrices, VertexConsumerProvider provider, int light, CallbackInfo ci) {
        ItemPhysic mod = getMod();
        if (mod == null || !mod.isEnabled()) return;

        boolean onGround = isTrulyOnGround(entity);

        // OnlyFalling: do nothing special for grounded items (vanilla bob/spin stays)
        if (onGround) {
            if (mod.onlyFalling()) {
                return;
            }
            // Flatten on ground when OnlyFalling is off
            matrices.translate(0, -0.05, 0);
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90f));
            return;
        }

        // In air: tip based on velocity — falls down, no age-based spin
        Vec3d vel = entity.getVelocity();
        float speed = mod.getRotateSpeed();
        float pitch = (float) MathHelper.clamp(-vel.y * 40.0 * speed, -75.0, 75.0);
        float roll = (float) MathHelper.clamp(vel.x * 25.0 * speed, -35.0, 35.0);

        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.yaw));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(pitch));
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(roll));
    }

    private static boolean isTrulyOnGround(ItemEntity entity) {
        if (entity.isOnGround()) return true;
        // Only treat as grounded if nearly stopped AND has lived long enough AND not in fluid
        Vec3d vel = entity.getVelocity();
        if (entity.age < 10) return false;
        if (entity.isTouchingWater()) return false;
        return Math.abs(vel.y) < 0.003
                && Math.abs(vel.x) < 0.003
                && Math.abs(vel.z) < 0.003;
    }

    private ItemPhysic getMod() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return null;
        for (var m : mm.getModules()) {
            if (m instanceof ItemPhysic) return (ItemPhysic) m;
        }
        return null;
    }
}
