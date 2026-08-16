package com.appetir.modules.impl;

import com.appetir.friends.FriendManager;
import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import net.minecraft.entity.player.PlayerEntity;

public class NoFriendDamage extends Module {

    private final BooleanSetting attack = new BooleanSetting("BlockAttack", "Cancel attacks on friends", true);

    public NoFriendDamage() {
        super("NoFriendDamage", "Не даёт бить друзей", Category.COMBAT);
        addSetting(attack);
    }

    public boolean shouldBlock() {
        return isEnabled() && attack.get();
    }

    /** @deprecated use FriendManager.isFriend(PlayerEntity) */
    public static boolean isFriend(String name) {
        FriendManager fm = FriendManager.getInstance();
        return fm != null && fm.isFriend(name);
    }

    public boolean isFriendPlayer(PlayerEntity player) {
        FriendManager fm = FriendManager.getInstance();
        return fm != null && fm.isFriend(player);
    }
}
