package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Hand;

import java.util.List;

public class AutoPotion extends Module {

    private final NumberSetting health = new NumberSetting("Health", "Drink below this HP", 10, 2, 20, 0.5);
    private int cooldown = 0;
    private int prevSlot = -1;
    private int potionSlot = -1;
    private boolean startedUse;

    public AutoPotion() {
        super("AutoPotion", "Пьёт healing/regen при низком HP", Category.COMBAT);
        addSetting(health);
    }

    @Override
    public void onDisable() {
        if (startedUse) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.player.isUsingItem()) {
                mc.player.stopUsingItem();
            }
        }
        startedUse = false;
        restoreSlot();
        cooldown = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (startedUse) {
            if (mc.player.isUsingItem()) return;
            // finished or cancelled
            startedUse = false;
            restoreSlot();
            cooldown = 20;
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (mc.player.getHealth() > health.getFloat()) return;
        if (mc.player.isUsingItem()) return;
        if (mc.player.hasStatusEffect(StatusEffects.REGENERATION)
                && mc.player.getStatusEffect(StatusEffects.REGENERATION).getAmplifier() >= 1) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStack(i);
            if (!isDrinkableHealing(stack)) continue;

            if (prevSlot < 0) prevSlot = mc.player.inventory.selectedSlot;
            potionSlot = i;
            mc.player.inventory.selectedSlot = i;
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            if (mc.player.isUsingItem()) {
                startedUse = true;
            } else {
                restoreSlot();
            }
            return;
        }
    }

    private void restoreSlot() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && prevSlot >= 0 && potionSlot >= 0
                && mc.player.inventory.selectedSlot == potionSlot) {
            mc.player.inventory.selectedSlot = prevSlot;
        }
        prevSlot = -1;
        potionSlot = -1;
    }

    private boolean isDrinkableHealing(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() != Items.POTION) return false;

        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
        boolean healing = false;
        for (StatusEffectInstance e : effects) {
            if (e.getEffectType() == StatusEffects.POISON
                    || e.getEffectType() == StatusEffects.INSTANT_DAMAGE
                    || e.getEffectType() == StatusEffects.WITHER) {
                return false;
            }
            if (e.getEffectType() == StatusEffects.INSTANT_HEALTH
                    || e.getEffectType() == StatusEffects.REGENERATION) {
                healing = true;
            }
        }
        return healing;
    }
}
