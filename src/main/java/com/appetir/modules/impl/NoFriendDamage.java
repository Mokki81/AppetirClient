package com.appetir.modules.impl;

import com.appetir.modules.Module;
import java.util.HashSet;
import java.util.Set;

// NoFriendDamage блокирует атаки по игрокам из whitelist.
// Реализуется через mixin на ClientPlayerInteractionManager#attackEntity
public class NoFriendDamage extends Module {

    public static final Set<String> friends = new HashSet<>();

    public NoFriendDamage() {
        super("NoFriendDamage", "Блокирует атаки по друзьям", Category.COMBAT);
    }

    public static void addFriend(String name)    { friends.add(name.toLowerCase()); }
    public static void removeFriend(String name) { friends.remove(name.toLowerCase()); }
    public static boolean isFriend(String name)  { return friends.contains(name.toLowerCase()); }
}
