package com.appetir.modules.impl;

import com.appetir.modules.Module;

// NoSlow убирает замедление при использовании предметов (еда, лук, щит).
// Реализуется через mixin на LivingEntity#getAttributeValue для скорости.
public class NoSlow extends Module {

    public NoSlow() {
        super("NoSlow", "Убирает замедление при использовании предметов", Category.MOVEMENT);
    }
}
