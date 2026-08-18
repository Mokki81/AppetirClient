package com.appetir.mixin;

import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.MiddleClick;
import com.appetir.util.BindManager;
import com.appetir.util.KeystrokesState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        KeystrokesState.onMouse(button, action);

        if (action != GLFW.GLFW_PRESS) return;

        // Bind listening can also capture mouse buttons as binds if desired
        if (BindManager.isListening()) {
            // Map mouse to negative-ish codes or skip — don't fire game actions
            ci.cancel();
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        // No middle-click game actions while any screen is open
        if (mc.currentScreen != null) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            ModuleManager mm = ModuleManager.getInstance();
            if (mm == null) return;
            for (Module m : mm.getModules()) {
                if (m instanceof MiddleClick && m.isEnabled()) {
                    ((MiddleClick) m).onMiddleClick();
                    break;
                }
            }
        }
    }
}
