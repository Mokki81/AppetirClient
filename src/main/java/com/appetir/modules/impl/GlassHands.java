package com.appetir.modules.impl;

import com.appetir.modules.Module;

// GlassHands скрывает модель руки в от первого лица.
// Реализуется через mixin на HeldItemRenderer — просто отменяем рендер.
public class GlassHands extends Module {
    public GlassHands() {
        super("GlassHands", "Прозрачные руки", Category.RENDER);
    }
}
