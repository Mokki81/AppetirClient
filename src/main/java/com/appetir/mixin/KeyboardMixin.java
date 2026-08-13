package com.appetir.mixin;

import com.appetir.AppetirClient;
import com.appetir.gui.AltManagerScreen;
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

        MinecraftClient mc = MinecraftClient.getInstance();

        // Right Shift — open / close ClickGUI
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (mc.currentScreen instanceof ClickGUI) {
                mc.setScreen(null);
            } else if (mc.currentScreen == null) {
                mc.setScreen(new ClickGUI());
            }
            return;
        }

        // Right Control — open Alt Manager
        if (key == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            if (mc.currentScreen instanceof AltManagerScreen) {
                mc.setScreen(null);
            } else {
                mc.setScreen(new AltManagerScreen(mc.currentScreen));
            }
            return;
        }

        // Right Alt — toggle HUD visibility
        if (key == GLFW.GLFW_KEY_RIGHT_ALT) {
            AppetirClient.hudVisible = !AppetirClient.hudVisible;
            return;
        }

        // Module keybinds (only when no screen is open)
        if (mc.currentScreen == null) {
            ModuleManager mm = ModuleManager.getInstance();
            if (mm != null) mm.onKeyPress(key);
        }
    }
}
