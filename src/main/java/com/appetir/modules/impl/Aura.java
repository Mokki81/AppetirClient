package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;
import com.appetir.util.Targeting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

public class Aura extends Module {

    private final NumberSetting range = new NumberSetting("Range", "Attack range", 4.5, 2.5, 6.0, 0.1);
    private final BooleanSetting players = new BooleanSetting("Players", "Attack players", true);
    private final BooleanSetting hostiles = new BooleanSetting("Hostiles", "Attack hostile mobs", true);
    private final BooleanSetting animals = new BooleanSetting("Animals", "Attack passive mobs", false);

    public Aura() {
        super("Aura", "Автоматически наводится и атакует выбранные цели", Category.COMBAT);
        addSetting(range);
        addSetting(players);
        addSetting(hostiles);
        addSetting(animals);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0) < 1.0f) return;

        Entity closest = null;
        double closest2 = range.get() * range.get();

        for (Entity e : mc.world.getEntities()) {
            if (!Targeting.isValidCombatTarget(e, players.get(), hostiles.get(), animals.get())) continue;
            double d = mc.player.squaredDistanceTo(e);
            if (d < closest2) {
                closest2 = d;
                closest = e;
            }
        }

        if (closest != null) {
            mc.player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, closest.getPos());
            mc.interactionManager.attackEntity(mc.player, closest);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
