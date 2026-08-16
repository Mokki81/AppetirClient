package com.appetir.modules.impl;

import com.appetir.modules.Module;

/** Removes block-breaking cooldown (see NoDelayMixin). */
public class NoDelay extends Module {
    public NoDelay() {
        super("NoDelay", "Убирает кулдаун ломания блоков", Category.MISC);
    }
}
