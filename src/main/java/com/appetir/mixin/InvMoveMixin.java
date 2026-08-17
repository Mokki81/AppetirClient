package com.appetir.mixin;

import com.appetir.gui.AltManagerScreen;
import com.appetir.gui.ClickGUI;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.InvMove;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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

        InvMove mod = getMod();
        if (mod == null || !mod.isEnabled()) return;
        if (!isInventoryLike(mc.currentScreen)) return;

        KeyboardInput self = (KeyboardInput) (Object) this;
        self.pressingForward = mc.options.keyForward.isPressed();
        self.pressingBack = mc.options.keyBack.isPressed();
        self.pressingLeft = mc.options.keyLeft.isPressed();
        self.pressingRight = mc.options.keyRight.isPressed();

        self.jumping = mod.allowJump() && mc.options.keyJump.isPressed();
        self.sneaking = mod.allowSneak() && mc.options.keySneak.isPressed();

        float x = 0, z = 0;
        if (self.pressingForward) z++;
        if (self.pressingBack) z--;
        if (self.pressingLeft) x++;
        if (self.pressingRight) x--;
        self.movementForward = z;
        self.movementSideways = x;
    }

    private static boolean isInventoryLike(Screen screen) {
        if (screen instanceof ChatScreen) return false;
        if (screen instanceof ClickGUI) return false;
        if (screen instanceof AltManagerScreen) return false;
        return screen instanceof HandledScreen;
    }

    private InvMove getMod() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return null;
        for (Module m : mm.getModules()) {
            if (m instanceof InvMove) return (InvMove) m;
        }
        return null;
    }
}
