package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.MiddleClick;
import com.appetir.util.KeystrokesState;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        KeystrokesState.onMouse(button, action);

        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            ModuleManager mm = ModuleManager.getInstance();
            if (mm == null) return;
            for (var m : mm.getModules()) {
                if (m instanceof MiddleClick && m.isEnabled()) {
                    ((MiddleClick) m).onMiddleClick();
                    break;
                }
            }
        }
    }
}
