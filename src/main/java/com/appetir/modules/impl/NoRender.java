package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;

public class NoRender extends Module {

    private final BooleanSetting fire = new BooleanSetting("Fire", "Hide fire overlay", true);
    private final BooleanSetting fog = new BooleanSetting("Fog", "Disable fog", true);
    private final BooleanSetting vignette = new BooleanSetting("Vignette", "Hide vignette", true);
    private final BooleanSetting pumpkin = new BooleanSetting("Pumpkin", "Hide pumpkin overlay", true);
    private final BooleanSetting totem = new BooleanSetting("Totem", "Hide totem animation", true);
    private final BooleanSetting bossBar = new BooleanSetting("BossBar", "Hide boss bars", false);
    private final BooleanSetting scoreboard = new BooleanSetting("Scoreboard", "Hide scoreboard", false);

    public NoRender() {
        super("NoRender", "Отключение лишнего рендера", Category.RENDER);
        addSetting(fire);
        addSetting(fog);
        addSetting(vignette);
        addSetting(pumpkin);
        addSetting(totem);
        addSetting(bossBar);
        addSetting(scoreboard);
    }

    public boolean noFire() { return isEnabled() && fire.get(); }
    public boolean noFog() { return isEnabled() && fog.get(); }
    public boolean noVignette() { return isEnabled() && vignette.get(); }
    public boolean noPumpkin() { return isEnabled() && pumpkin.get(); }
    public boolean noTotem() { return isEnabled() && totem.get(); }
    public boolean noBossBar() { return isEnabled() && bossBar.get(); }
    public boolean noScoreboard() { return isEnabled() && scoreboard.get(); }
}
