package com.appetir.mixin;

import com.appetir.AppetirClient;
import com.appetir.client.ClientMode;
import com.appetir.config.ConfigManager;
import com.appetir.gui.AltManagerScreen;
import com.appetir.gui.ClickGUI;
import com.appetir.modules.ModuleManager;
import com.appetir.util.BindManager;
import com.appetir.util.KeystrokesState;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        KeystrokesState.onKey(key, action);

        if (action != GLFW.GLFW_PRESS) return;

        if (BindManager.isListening()) {
            if (BindManager.onKey(key)) ci.cancel();
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (mc.currentScreen instanceof ClickGUI) {
                mc.setScreen(null);
            } else if (mc.currentScreen == null || mc.currentScreen instanceof AltManagerScreen) {
                mc.setScreen(new ClickGUI());
            }
            ci.cancel();
            return;
        }

        if (key == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            if (ClientMode.isClean()) {
                ci.cancel();
                return;
            }
            if (mc.currentScreen instanceof AltManagerScreen) {
                mc.setScreen(null);
            } else {
                mc.setScreen(new AltManagerScreen(mc.currentScreen));
            }
            ci.cancel();
            return;
        }

        if (key == GLFW.GLFW_KEY_RIGHT_ALT) {
            AppetirClient.hudVisible = !AppetirClient.hudVisible;
            ConfigManager cm = ConfigManager.getInstance();
            if (cm != null) cm.markDirty();
            ci.cancel();
            return;
        }

        if (key == GLFW.GLFW_KEY_INSERT) {
            ClientMode.toggle();
            ci.cancel();
            return;
        }

        if (mc.currentScreen == null) {
            ModuleManager mm = ModuleManager.getInstance();
            if (mm != null) mm.onKeyPress(key);
        }
    }
}
