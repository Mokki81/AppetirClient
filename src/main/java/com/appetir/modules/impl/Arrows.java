package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;

public class Arrows extends Module {

    private final BooleanSetting playersOnly = new BooleanSetting("PlayersOnly", "Only players", true);
    private final NumberSetting range = new NumberSetting("Range", "Max distance", 64, 16, 128, 8);

    public Arrows() {
        super("Arrows", "Линии к игрокам", Category.RENDER);
        addSetting(playersOnly);
        addSetting(range);
    }

    public boolean playersOnly() { return playersOnly.get(); }
    public double getRange() { return range.get(); }
}
