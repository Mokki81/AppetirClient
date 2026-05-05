package com.appetir.modules.impl;

import com.appetir.modules.Module;

// MiddleClick — действия по средней кнопке мыши:
// клик по сущности → скопировать её тип в хотбар (как в креативе).
// Реализуется через mixin на Mouse::onMouseButton.
public class MiddleClick extends Module {
    public MiddleClick() {
        super("MiddleClick", "Действия по средней кнопке мыши", Category.MISC);
    }
}
