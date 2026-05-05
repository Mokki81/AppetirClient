package com.appetir.modules.impl;

import com.appetir.modules.Module;

// NoPush отключает толкание игрока другими сущностями.
// Реализуется через mixin на Entity::pushAwayFrom.
public class NoPush extends Module {
    public NoPush() {
        super("NoPush", "Отключение толкания", Category.MISC);
    }
}
