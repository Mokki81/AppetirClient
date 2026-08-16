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
 * Items: tip while falling based on velocity, lie flat on ground.
 * No continuous spin-in-place.
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

        Vec3d vel = entity.getVelocity();
        boolean onGround = entity.isOnGround() || Math.abs(vel.y) < 0.01 && entity.age > 5;

        if (onGround && mod.onlyFalling()) {
            // Flat on the ground (90° tip onto X) — stable, no spin
            matrices.translate(0, -0.05, 0);
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90f));
            return;
        }

        if (onGround) {
            matrices.translate(0, -0.05, 0);
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90f));
            return;
        }

        // In air: gentle pitch from vertical velocity (falls down, tips forward)
        float speed = mod.getRotateSpeed();
        float pitch = (float) MathHelper.clamp(-vel.y * 40.0 * speed, -75.0, 75.0);
        float roll = (float) MathHelper.clamp(vel.x * 25.0 * speed, -35.0, 35.0);

        // Stable yaw from entity — not age-based spinning
        float yRot = entity.yaw;

        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(yRot));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(pitch));
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(roll));
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
