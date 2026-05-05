package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

// AspectRatio меняет соотношение сторон рендера через mixin на Window.
public class AspectRatio extends Module {

    public static float ratio = 16.0f / 9.0f; // можно менять в настройках

    public AspectRatio() {
        super("AspectRatio", "Соотношение сторон экрана", Category.RENDER);
    }
}
