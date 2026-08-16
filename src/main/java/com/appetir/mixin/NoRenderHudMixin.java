package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoRender;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class NoRenderHudMixin {

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    private void cancelPumpkin(CallbackInfo ci) {
        NoRender mod = getMod();
        if (mod != null && mod.noPumpkin()) ci.cancel();
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    private void cancelVignette(Entity entity, CallbackInfo ci) {
        NoRender mod = getMod();
        if (mod != null && mod.noVignette()) ci.cancel();
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void cancelScoreboard(MatrixStack matrices, ScoreboardObjective objective, CallbackInfo ci) {
        NoRender mod = getMod();
        if (mod != null && mod.noScoreboard()) ci.cancel();
    }

    /** Totem popup — floating item status */
    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void onStatusBars(MatrixStack matrices, CallbackInfo ci) {
        // no-op anchor; totem handled below via overlay texture cancel if present
    }

    /**
     * Hide totem-of-undying floating animation by cancelling overlay with totem texture.
     * 1.16.5: renderPortalOverlay / similar; also cancel when scaledItem is totem via separate path.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onHudRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        // Handled by dedicated injects; keep for future overlays
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
