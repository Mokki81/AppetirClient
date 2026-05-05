package com.appetir.modules.impl;

import com.appetir.modules.Module;

// Arrows рисует стрелки над игроками через mixin на WorldRenderer.
// Состояние читается из isEnabled().
public class Arrows extends Module {
    public Arrows() {
        super("Arrows", "Показывает стрелки на игроках", Category.RENDER);
    }
}
