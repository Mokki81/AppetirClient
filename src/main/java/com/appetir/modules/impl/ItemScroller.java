package com.appetir.modules.impl;

import com.appetir.modules.Module;

// ItemScroller ускоряет скроллинг предметов в инвентаре.
// Реализуется через mixin на Mouse::onMouseScroll.
public class ItemScroller extends Module {

    public static int scrollSpeed = 3; // предметов за одно движение колеса

    public ItemScroller() {
        super("ItemScroller", "Скроллинг предметов", Category.MISC);
    }
}
