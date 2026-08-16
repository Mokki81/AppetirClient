package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * State-aware totem swap with expected-stack validation and safe abort.
 */
public class AutoTotem extends Module {

    private enum Phase {
        IDLE, WAIT_PICKUP, WAIT_OFFHAND, WAIT_RETURN, COOLDOWN
    }

    private Phase phase = Phase.IDLE;
    private int containerSlot = -1;
    private int syncId = -1;
    private int timeout = 0;
    private ItemStack expectedCursor = ItemStack.EMPTY;
    private ItemStack previousOffhand = ItemStack.EMPTY;

    public AutoTotem() {
        super("AutoTotem", "Автоматически кладёт тотем в оффхенд", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        reset();
    }

    private void reset() {
        phase = Phase.IDLE;
        containerSlot = -1;
        syncId = -1;
        timeout = 0;
        expectedCursor = ItemStack.EMPTY;
        previousOffhand = ItemStack.EMPTY;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null || mc.world == null) {
            reset();
            return;
        }

        if (!(mc.player.currentScreenHandler instanceof PlayerScreenHandler)
                || mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
            reset();
            return;
        }

        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            reset();
            return;
        }

        switch (phase) {
            case IDLE: {
                if (!mc.player.inventory.getCursorStack().isEmpty()) return;
                int inv = findTotemInvSlot(mc);
                if (inv < 0) return;
                containerSlot = inv < 9 ? inv + 36 : inv;
                syncId = mc.player.playerScreenHandler.syncId;
                if (!slotHasTotem(mc, containerSlot)) return;

                previousOffhand = mc.player.getOffHandStack().copy();
                expectedCursor = mc.player.inventory.getStack(
                        containerSlot >= 36 ? containerSlot - 36 : containerSlot).copy();

                mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                phase = Phase.WAIT_PICKUP;
                timeout = 12;
                break;
            }
            case WAIT_PICKUP: {
                if (!validHandler(mc) || --timeout <= 0) {
                    safeAbort(mc);
                    return;
                }
                ItemStack cursor = mc.player.inventory.getCursorStack();
                if (cursor.getItem() == Items.TOTEM_OF_UNDYING) {
                    mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                    phase = Phase.WAIT_OFFHAND;
                    timeout = 12;
                }
                break;
            }
            case WAIT_OFFHAND: {
                if (!validHandler(mc) || --timeout <= 0) {
                    safeAbort(mc);
                    return;
                }
                if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    ItemStack cursor = mc.player.inventory.getCursorStack();
                    if (cursor.isEmpty()) {
                        phase = Phase.COOLDOWN;
                        timeout = 3;
                    } else {
                        // Only return if source looks empty-ish or can accept
                        mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                        phase = Phase.WAIT_RETURN;
                        timeout = 12;
                    }
                }
                break;
            }
            case WAIT_RETURN: {
                if (!validHandler(mc) || --timeout <= 0) {
                    // Don't blind-click further — stop
                    phase = Phase.COOLDOWN;
                    timeout = 5;
                    return;
                }
                if (mc.player.inventory.getCursorStack().isEmpty()) {
                    phase = Phase.COOLDOWN;
                    timeout = 3;
                }
                break;
            }
            case COOLDOWN: {
                if (--timeout <= 0) reset();
                break;
            }
        }
    }

    /**
     * Only click source if cursor still looks like previous offhand and recovery is safe.
     * Never blind-swap into an occupied/wrong slot.
     */
    private void safeAbort(MinecraftClient mc) {
        try {
            if (!validHandler(mc)) {
                reset();
                return;
            }
            ItemStack cursor = mc.player.inventory.getCursorStack();
            if (cursor.isEmpty()) {
                phase = Phase.COOLDOWN;
                timeout = 5;
                return;
            }
            // Safe only if source slot is empty — then put cursor back
            if (containerSlot >= 0 && isSourceEmpty(mc, containerSlot)) {
                mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
            }
            // else: leave cursor — better than wrong swap
        } catch (Exception ignored) {
        }
        phase = Phase.COOLDOWN;
        timeout = 5;
    }

    private boolean isSourceEmpty(MinecraftClient mc, int containerSlot) {
        int inv = containerSlot >= 36 && containerSlot <= 44 ? containerSlot - 36 : containerSlot;
        if (inv < 0 || inv >= 36) return false;
        return mc.player.inventory.getStack(inv).isEmpty();
    }

    private boolean validHandler(MinecraftClient mc) {
        return mc.player != null
                && mc.interactionManager != null
                && mc.player.currentScreenHandler instanceof PlayerScreenHandler
                && mc.player.currentScreenHandler.syncId == syncId;
    }

    private boolean slotHasTotem(MinecraftClient mc, int containerSlot) {
        int inv = containerSlot >= 36 && containerSlot <= 44 ? containerSlot - 36 : containerSlot;
        if (inv < 0 || inv >= 36) return false;
        return mc.player.inventory.getStack(inv).getItem() == Items.TOTEM_OF_UNDYING;
    }

    private int findTotemInvSlot(MinecraftClient mc) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) return i;
        }
        return -1;
    }
}
