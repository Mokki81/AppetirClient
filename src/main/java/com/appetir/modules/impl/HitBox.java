package com.appetir.modules.impl;

import com.appetir.modules.Module;

// HitBox расширяет хитбоксы сущностей.
// Реализуется через mixin на Entity#getBoundingBox()
// Этот класс управляет только состоянием.
public class HitBox extends Module {

    public static float expansion = 0.3f;

    public HitBox() {
        super("HitBox", "Увеличивает хитбоксы целей, облегчая попадание по ним", Category.COMBAT);
    }
}
