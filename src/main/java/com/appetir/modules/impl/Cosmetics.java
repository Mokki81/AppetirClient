package com.appetir.modules.impl;

import com.appetir.modules.Module;

// Cosmetics добавляет косметику: шляпу, следы, круги при прыжке.
// Рендер через mixin на PlayerEntityRenderer.
public class Cosmetics extends Module {

    public static boolean showHat    = true;
    public static boolean showTrail  = true;
    public static boolean jumpCircle = true;

    public Cosmetics() {
        super("Cosmetics", "Косметика (шляпа, следы, круги при прыжке)", Category.RENDER);
    }
}
