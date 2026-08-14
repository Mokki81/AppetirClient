package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;

public class InvMove extends Module {

    private final BooleanSetting sneak = new BooleanSetting("Sneak", "Allow sneak in GUI", true);
    private final BooleanSetting jump = new BooleanSetting("Jump", "Allow jump in GUI", true);

    public InvMove() {
        super("InvMove", "Движение в инвентаре", Category.MOVEMENT);
        addSetting(sneak);
        addSetting(jump);
    }

    public boolean allowSneak() { return isEnabled() && sneak.get(); }
    public boolean allowJump() { return isEnabled() && jump.get(); }
}
