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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * push/pop around transforms so matrix state never leaks to the next entity.
 * Ground detection prefers isOnGround() — velocity-only heuristic removed.
 */
@Mixin(ItemEntityRenderer.class)
public class ItemPhysicMixin {

    @Unique
    private boolean appetir$pushed;

    @Inject(
            method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void onRenderHead(ItemEntity entity, float yaw, float tickDelta,
                              MatrixStack matrices, VertexConsumerProvider provider, int light, CallbackInfo ci) {
        appetir$pushed = false;
        ItemPhysic mod = getMod();
        if (mod == null || !mod.isEnabled()) return;

        boolean onGround = entity.isOnGround();

        if (onGround) {
            if (mod.onlyFalling()) return; // leave vanilla pose
            matrices.push();
            appetir$pushed = true;
            matrices.translate(0, -0.05, 0);
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90f));
            return;
        }

        Vec3d vel = entity.getVelocity();
        float speed = mod.getRotateSpeed();
        float pitch = (float) MathHelper.clamp(-vel.y * 40.0 * speed, -75.0, 75.0);
        float roll = (float) MathHelper.clamp(vel.x * 25.0 * speed, -35.0, 35.0);

        matrices.push();
        appetir$pushed = true;
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.yaw));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(pitch));
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(roll));
    }

    @Inject(
            method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("RETURN")
    )
    private void onRenderReturn(ItemEntity entity, float yaw, float tickDelta,
                                MatrixStack matrices, VertexConsumerProvider provider, int light, CallbackInfo ci) {
        if (appetir$pushed) {
            matrices.pop();
            appetir$pushed = false;
        }
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
