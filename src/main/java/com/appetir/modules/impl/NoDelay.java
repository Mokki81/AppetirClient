package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

// NoDelay убирает задержку между использованием предметов.
// Реализуется через mixin на ClientPlayerInteractionManager.
public class NoDelay extends Module {
    public NoDelay() {
        super("NoDelay", "Убирает задержки", Category.MISC);
    }
}
