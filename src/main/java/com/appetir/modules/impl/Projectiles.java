package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;

public class Projectiles extends Module {

    private final BooleanSetting arrows = new BooleanSetting("Arrows", "Show arrows", true);
    private final BooleanSetting pearls = new BooleanSetting("Pearls", "Show ender pearls", true);
    private final BooleanSetting snowballs = new BooleanSetting("Snowballs", "Show snowballs", true);

    public Projectiles() {
        super("Projectiles", "ESP снарядов", Category.RENDER);
        addSetting(arrows);
        addSetting(pearls);
        addSetting(snowballs);
    }

    public boolean showArrows() { return arrows.get(); }
    public boolean showPearls() { return pearls.get(); }
    public boolean showSnowballs() { return snowballs.get(); }
}
