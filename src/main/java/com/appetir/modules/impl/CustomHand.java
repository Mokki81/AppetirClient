package com.appetir.modules.impl;

import com.appetir.modules.Module;

// CustomHand меняет позицию, масштаб и угол руки.
// Реализуется через mixin на HeldItemRenderer.
public class CustomHand extends Module {

    public static float scaleX    = 1.0f;
    public static float scaleY    = 1.0f;
    public static float scaleZ    = 1.0f;
    public static float offsetX   = 0.0f;
    public static float offsetY   = 0.0f;
    public static float offsetZ   = 0.0f;
    public static float rotationX = 0.0f;
    public static float rotationY = 0.0f;

    public CustomHand() {
        super("CustomHand", "Кастомизация рук", Category.RENDER);
    }
}
