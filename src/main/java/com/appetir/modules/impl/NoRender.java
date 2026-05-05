package com.appetir.modules.impl;

import com.appetir.modules.Module;

// NoRender отключает рендер выбранных элементов мира.
// Каждый флаг читается из соответствующего mixin.
public class NoRender extends Module {

    public static boolean noFire      = true;
    public static boolean noFog       = true;
    public static boolean noVignette  = true;
    public static boolean noPumpkin   = true;  // тыква на голове
    public static boolean noTotemAnim = true;  // анимация тотема
    public static boolean noOverlay   = false; // оверлей воды/лавы

    public NoRender() {
        super("NoRender", "Отключение рендера элементов", Category.RENDER);
    }
}
