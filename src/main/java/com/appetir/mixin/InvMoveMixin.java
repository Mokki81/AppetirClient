package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.InvMove;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class InvMoveMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(boolean slowDown, float f, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen == null) return;
        if (!isEnabled()) return;

        KeyboardInput self = (KeyboardInput)(Object)this;
        self.pressingForward  = mc.options.keyForward.isPressed();
        self.pressingBack     = mc.options.keyBack.isPressed();
        self.pressingLeft     = mc.options.keyLeft.isPressed();
        self.pressingRight    = mc.options.keyRight.isPressed();
        self.jumping          = mc.options.keyJump.isPressed();
        self.sneaking         = mc.options.keySneak.isPressed();

        float x = 0, z = 0;
        if (self.pressingForward) z++;
        if (self.pressingBack)    z--;
        if (self.pressingLeft)    x++;
        if (self.pressingRight)   x--;
        self.movementForward  = z;
        self.movementSideways = x;
    }

    private boolean isEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        return mm.getModules().stream()
            .filter(m -> m instanceof InvMove)
            .anyMatch(m -> m.isEnabled());
    }
}
