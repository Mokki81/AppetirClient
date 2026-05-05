package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.Cosmetics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class CosmeticsMixin {

    private static int tick = 0;

    @Inject(method = "render(Lnet/minecraft/entity/player/PlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL"))
    private void onRender(PlayerEntity player, float yaw, float tickDelta,
                          MatrixStack matrices, VertexConsumerProvider provider, int light, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world==null||player!=mc.player||!isEnabled()) return;
        tick++;

        if (Cosmetics.showTrail && tick%3==0 && player.isOnGround()
                && player.getVelocity().horizontalLength()>0.05) {
            mc.world.addParticle(ParticleTypes.WITCH,
                player.getX()+(Math.random()-0.5)*0.3,
                player.getY()+0.1,
                player.getZ()+(Math.random()-0.5)*0.3,
                0,0.05,0);
        }

        if (Cosmetics.jumpCircle && player.getVelocity().getY()>0.3 && !player.isOnGround()) {
            for (int i=0; i<12; i++) {
                double a=(2*Math.PI/12)*i;
                mc.world.addParticle(ParticleTypes.END_ROD,
                    player.getX()+Math.cos(a)*0.6, player.getY(), player.getZ()+Math.sin(a)*0.6,
                    0,0,0);
            }
        }
    }

    private boolean isEnabled() {
        ModuleManager mm=ModuleManager.getInstance();
        if (mm==null) return false;
        return mm.getModules().stream().filter(m->m instanceof Cosmetics).anyMatch(m->m.isEnabled());
    }
}
