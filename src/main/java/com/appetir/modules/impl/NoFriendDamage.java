package com.appetir.modules.impl;

import com.appetir.friends.FriendManager;
import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;

public class NoFriendDamage extends Module {

    private final BooleanSetting attack = new BooleanSetting("BlockAttack", "Cancel attacks on friends", true);

    public NoFriendDamage() {
        super("NoFriendDamage", "Не даёт бить друзей", Category.COMBAT);
        addSetting(attack);
    }

    public boolean shouldBlock() {
        return isEnabled() && attack.get();
    }

    public boolean isFriend(String name) {
        FriendManager fm = FriendManager.getInstance();
        return fm != null && fm.isFriend(name);
    }
}
