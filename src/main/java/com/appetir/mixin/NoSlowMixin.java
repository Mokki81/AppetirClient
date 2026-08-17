package com.appetir.mixin;

import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoSlow;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientPlayerEntity.class)
public class NoSlowMixin {

    @ModifyConstant(method = "tickMovement", constant = @Constant(floatValue = 0.2F))
    private float appetir$noItemSlow(float original) {
        if (isNoSlowItems()) return 1.0F;
        return original;
    }

    private boolean isNoSlowItems() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        for (Module m : mm.getModules()) {
            if (m instanceof NoSlow && m.isEnabled()) {
                return ((NoSlow) m).items();
            }
        }
        return false;
    }
}
