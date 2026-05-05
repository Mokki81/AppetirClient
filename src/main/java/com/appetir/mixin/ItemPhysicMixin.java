package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.ItemPhysic;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemPhysicMixin {

    @Inject(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"))
    private void onRender(ItemEntity entity, float yaw, float tickDelta,
                          MatrixStack matrices, VertexConsumerProvider provider, int light, CallbackInfo ci) {
        if (!isEnabled()) return;
        float age = entity.getItemAge() + tickDelta;
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(age * 3.0f));
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float)Math.sin(age * 0.1f) * 30f));
    }

    private boolean isEnabled() {
        ModuleManager mm=ModuleManager.getInstance();
        if (mm==null) return false;
        return mm.getModules().stream().filter(m->m instanceof ItemPhysic).anyMatch(m->m.isEnabled());
    }
}
