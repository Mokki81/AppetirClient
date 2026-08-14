package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;

public class NameTags extends Module {

    private final BooleanSetting health = new BooleanSetting("Health", "Show HP", true);
    private final BooleanSetting distance = new BooleanSetting("Distance", "Show distance", true);
    private final BooleanSetting armor = new BooleanSetting("Armor", "Show armor points", false);
    private final NumberSetting scale = new NumberSetting("Scale", "Tag scale", 1.2, 0.5, 3.0, 0.1);

    public NameTags() {
        super("NameTags", "Улучшенные теги игроков", Category.RENDER);
        addSetting(health);
        addSetting(distance);
        addSetting(armor);
        addSetting(scale);
    }

    public boolean showHealth() { return health.get(); }
    public boolean showDistance() { return distance.get(); }
    public boolean showArmor() { return armor.get(); }
    public float getScale() { return scale.getFloat(); }
}
