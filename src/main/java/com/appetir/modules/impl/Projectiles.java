package com.appetir.modules.impl;

import com.appetir.modules.Module;

// Projectiles показывает траектории снарядов (стрелы, снежки, файерболы).
// Рендер через mixin на WorldRenderer::render — рисуем линии по пути снаряда.
public class Projectiles extends Module {

    public static boolean showArrows     = true;
    public static boolean showSnowballs  = true;
    public static boolean showFireballs  = true;
    public static int     trailLength    = 20; // тиков

    public Projectiles() {
        super("Projectiles", "Траектории снарядов", Category.RENDER);
    }
}
