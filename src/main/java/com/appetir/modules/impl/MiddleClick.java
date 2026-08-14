package com.appetir.modules.impl;

import com.appetir.friends.FriendManager;
import com.appetir.modules.Module;
import com.appetir.settings.ModeSetting;
import com.appetir.util.NotificationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class MiddleClick extends Module {

    private final ModeSetting action = new ModeSetting("Action", "Middle click action", "Friend",
            "Friend", "None");

    public MiddleClick() {
        super("MiddleClick", "СКМ по игроку = друг", Category.MISC);
        addSetting(action);
    }

    /** Call from mouse mixin when middle button pressed */
    public void onMiddleClick() {
        if (!isEnabled() || action.is("None")) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (!(entity instanceof PlayerEntity)) return;

        String name = entity.getEntityName();
        FriendManager fm = FriendManager.getInstance();
        if (fm == null) return;

        if (fm.isFriend(name)) {
            fm.remove(name);
            NotificationManager.push("Friend", "Removed " + name);
        } else {
            fm.add(name);
            NotificationManager.push("Friend", "Added " + name);
        }
    }
}
