package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoRender;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancel totem pop animation (floating item) when NoRender.Totem is on.
 */
@Mixin(InGameHud.class)
public class NoRenderTotemMixin {

    @Shadow private ItemStack currentStack;
    @Shadow private int heldItemTooltipFade;

    @Inject(method = "renderFloatingItem", at = @At("HEAD"), cancellable = true)
    private void cancelTotemFloat(int scaledWidth, int scaledHeight, float tickDelta, CallbackInfo ci) {
        NoRender mod = getMod();
        if (mod == null || !mod.noTotem()) return;
        if (currentStack != null && currentStack.getItem() == Items.TOTEM_OF_UNDYING) {
            ci.cancel();
        }
    }

    private static NoRender getMod() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return null;
        for (var m : mm.getModules()) {
            if (m instanceof NoRender) return (NoRender) m;
        }
        return null;
    }
}
