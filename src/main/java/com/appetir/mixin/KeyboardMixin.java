package com.appetir.mixin;

import com.appetir.AppetirClient;
import com.appetir.gui.ClickGUI;
import com.appetir.modules.ModuleManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action != GLFW.GLFW_PRESS) return;

        // Right Shift — открыть ClickGUI (как на скрине)
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            MinecraftClient.getInstance().setScreen(new ClickGUI());
            return;
        }

        // Right Alt — переключить HUD
        if (key == GLFW.GLFW_KEY_RIGHT_ALT) {
            AppetirClient.hudVisible = !AppetirClient.hudVisible;
            return;
        }

        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null) mm.onKeyPress(key);
    }
}
