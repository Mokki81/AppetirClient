package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * State machine swap: one click per tick to reduce inventory desync.
 */
public class AutoTotem extends Module {

    private enum Phase {
        IDLE, PICKUP, TO_OFFHAND, RETURN, COOLDOWN
    }

    private Phase phase = Phase.IDLE;
    private int containerSlot = -1;
    private int syncId = -1;
    private int waitTicks = 0;

    public AutoTotem() {
        super("AutoTotem", "Автоматически кладёт тотем в оффхенд", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        phase = Phase.IDLE;
        containerSlot = -1;
        syncId = -1;
        waitTicks = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null || mc.world == null) {
            phase = Phase.IDLE;
            return;
        }

        if (!(mc.player.currentScreenHandler instanceof PlayerScreenHandler)) {
            phase = Phase.IDLE;
            return;
        }
        if (mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
            phase = Phase.IDLE;
            return;
        }

        // Already has totem
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            phase = Phase.IDLE;
            return;
        }

        if (waitTicks > 0) {
            waitTicks--;
            return;
        }

        switch (phase) {
            case IDLE:
                if (!mc.player.inventory.getCursorStack().isEmpty()) return;
                int inv = findTotemInvSlot(mc);
                if (inv < 0) return;
                containerSlot = inv < 9 ? inv + 36 : inv;
                syncId = mc.player.playerScreenHandler.syncId;
                phase = Phase.PICKUP;
                // fall through same tick is ok for first step
                // no break — do first click

            case PICKUP:
                if (!validHandler(mc)) { phase = Phase.IDLE; return; }
                mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                phase = Phase.TO_OFFHAND;
                waitTicks = 1;
                break;

            case TO_OFFHAND:
                if (!validHandler(mc)) { phase = Phase.IDLE; return; }
                // Abort if cursor somehow empty (desync)
                if (mc.player.inventory.getCursorStack().isEmpty()
                        && mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                    phase = Phase.IDLE;
                    return;
                }
                mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                phase = Phase.RETURN;
                waitTicks = 1;
                break;

            case RETURN:
                if (!validHandler(mc)) { phase = Phase.IDLE; return; }
                if (!mc.player.inventory.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                }
                phase = Phase.COOLDOWN;
                waitTicks = 2;
                break;

            case COOLDOWN:
                phase = Phase.IDLE;
                containerSlot = -1;
                break;
        }
    }

    private boolean validHandler(MinecraftClient mc) {
        return mc.player != null
                && mc.interactionManager != null
                && mc.player.currentScreenHandler instanceof PlayerScreenHandler
                && mc.player.currentScreenHandler.syncId == syncId;
    }

    private int findTotemInvSlot(MinecraftClient mc) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) return i;
        }
        return -1;
    }
}
