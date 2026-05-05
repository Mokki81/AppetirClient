package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

// InvMove — движение при открытом инвентаре.
// Реализуется через mixin на KeyboardInput, чтобы клавиши WASD работали даже в GUI.
public class InvMove extends Module {

    public InvMove() {
        super("InvMove", "Позволяет двигаться при открытом инвентаре", Category.MOVEMENT);
    }
}
