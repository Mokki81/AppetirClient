package com.appetir.modules.impl;

import com.appetir.friends.FriendManager;
import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;

public class ESP extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "ESP style", "Box", "Box", "Outline");
    private final BooleanSetting players = new BooleanSetting("Players", "Show players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", "Show mobs", false);
    private final BooleanSetting friends = new BooleanSetting("Friends", "Highlight friends", true);
    private final NumberSetting range = new NumberSetting("Range", "Max render distance", 64, 16, 128, 8);

    public ESP() {
        super("ESP", "Видит сущности сквозь стены", Category.RENDER);
        addSetting(mode);
        addSetting(players);
        addSetting(mobs);
        addSetting(friends);
        addSetting(range);
    }

    // Actual rendering is handled in WorldRendererMixin / mixins that check isEnabled()
    public boolean showPlayers() { return players.get(); }
    public boolean showMobs() { return mobs.get(); }
    public boolean showFriends() { return friends.get(); }
    public double getRange() { return range.get(); }
    public String getMode() { return mode.get(); }

    public boolean isFriend(String name) {
        return FriendManager.getInstance() != null && FriendManager.getInstance().isFriend(name);
    }
}
