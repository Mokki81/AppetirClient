package com.appetir.mixin;

import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoRender;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class NoRenderTotemMixin {

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void cancelTotemFloat(ItemStack floatingItem, CallbackInfo ci) {
        NoRender mod = getMod();
        if (mod == null || !mod.noTotem()) return;
        if (floatingItem != null && floatingItem.getItem() == Items.TOTEM_OF_UNDYING) {
            ci.cancel();
        }
    }

    private static NoRender getMod() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return null;
        for (Module m : mm.getModules()) {
            if (m instanceof NoRender) return (NoRender) m;
        }
        return null;
    }
}
