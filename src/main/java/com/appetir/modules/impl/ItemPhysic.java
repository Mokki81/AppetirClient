package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;

/**
 * Visual item physics — items tip while falling, rest flat on ground.
 */
public class ItemPhysic extends Module {

    private final NumberSetting rotateSpeed = new NumberSetting("RotateSpeed", "Fall tip speed", 1.5, 0.2, 4.0, 0.1);
    private final BooleanSetting onlyFalling = new BooleanSetting("OnlyFalling", "Animate only in air", true);

    public ItemPhysic() {
        super("ItemPhysic", "Реалистичное падение предметов", Category.RENDER);
        addSetting(rotateSpeed);
        addSetting(onlyFalling);
    }

    public float getRotateSpeed() { return rotateSpeed.getFloat(); }
    public boolean onlyFalling() { return onlyFalling.get(); }
}
