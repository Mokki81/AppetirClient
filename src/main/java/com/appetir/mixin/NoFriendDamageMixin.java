package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoFriendDamage;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class NoFriendDamageMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        boolean enabled = mm.getModules().stream()
            .filter(m -> m instanceof NoFriendDamage)
            .anyMatch(m -> m.isEnabled());

        if (!enabled) return;
        if (!(target instanceof PlayerEntity)) return;

        String name = ((PlayerEntity) target).getGameProfile().getName();
        if (NoFriendDamage.isFriend(name)) {
            ci.cancel();
        }
    }
}
