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

    public AutoPotion() {
        super("AutoPotion", "Пьёт healing/regen при низком HP", Category.COMBAT);
        addSetting(health);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0 && prevSlot >= 0) {
                mc.player.inventory.selectedSlot = prevSlot;
                prevSlot = -1;
            }
            return;
        }
        if (mc.player.getHealth() > health.getFloat()) return;
        // Already regenerating heavily
        if (mc.player.hasStatusEffect(StatusEffects.REGENERATION)
                && mc.player.getStatusEffect(StatusEffects.REGENERATION).getAmplifier() >= 1) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStack(i);
            if (!isHealingPotion(stack)) continue;

            prevSlot = mc.player.inventory.selectedSlot;
            mc.player.inventory.selectedSlot = i;
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            cooldown = 25;
            return;
        }
    }

    private boolean isHealingPotion(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() != Items.POTION && stack.getItem() != Items.SPLASH_POTION
                && stack.getItem() != Items.LINGERING_POTION) return false;

        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
        for (StatusEffectInstance e : effects) {
            if (e.getEffectType() == StatusEffects.INSTANT_HEALTH) return true;
            if (e.getEffectType() == StatusEffects.REGENERATION) return true;
            // reject harmful
            if (e.getEffectType() == StatusEffects.POISON) return false;
            if (e.getEffectType() == StatusEffects.INSTANT_DAMAGE) return false;
            if (e.getEffectType() == StatusEffects.WITHER) return false;
        }
        return false;
    }
}
