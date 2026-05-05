package com.appetir.modules.impl;

import com.appetir.modules.Module;

// NameTags показывает теги над игроками с ХП и дистанцией.
// Рендер через mixin на PlayerEntityRenderer.
public class NameTags extends Module {

    public static boolean showHealth   = true;
    public static boolean showDistance = true;
    public static float   scale        = 1.5f;

    public NameTags() {
        super("NameTags", "Теги над игроками", Category.RENDER);
    }
}
