package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * State machine with stack-based confirmation between clicks.
 */
public class AutoTotem extends Module {

    private enum Phase {
        IDLE, WAIT_PICKUP, TO_OFFHAND, WAIT_OFFHAND, RETURN, WAIT_RETURN, COOLDOWN
    }

    private Phase phase = Phase.IDLE;
    private int containerSlot = -1;
    private int syncId = -1;
    private int waitTicks = 0;
    private int stallTicks = 0;
    private static final int MAX_STALL = 20;

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
        waitTicks = 0;
        stallTicks = 0;
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
            if (phase != Phase.IDLE && phase != Phase.COOLDOWN) reset();
            return;
        }

        if (waitTicks > 0) {
            waitTicks--;
            return;
        }

        switch (phase) {
            case IDLE: {
                if (!mc.player.inventory.getCursorStack().isEmpty()) return;
                int inv = findTotemInvSlot(mc);
                if (inv < 0) return;
                containerSlot = inv < 9 ? inv + 36 : inv;
                // Verify slot still holds totem
                if (!slotIsTotem(mc, inv)) return;
                syncId = mc.player.playerScreenHandler.syncId;
                mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                phase = Phase.WAIT_PICKUP;
                stallTicks = 0;
                break;
            }

            case WAIT_PICKUP: {
                if (!validHandler(mc)) { reset(); return; }
                ItemStack cursor = mc.player.inventory.getCursorStack();
                if (cursor.getItem() == Items.TOTEM_OF_UNDYING) {
                    phase = Phase.TO_OFFHAND;
                    stallTicks = 0;
                } else if (++stallTicks > MAX_STALL) {
                    reset();
                }
                break;
            }

            case TO_OFFHAND: {
                if (!validHandler(mc)) { reset(); return; }
                if (mc.player.inventory.getCursorStack().getItem() != Items.TOTEM_OF_UNDYING) {
                    reset();
                    return;
                }
                mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                phase = Phase.WAIT_OFFHAND;
                stallTicks = 0;
                break;
            }

            case WAIT_OFFHAND: {
                if (!validHandler(mc)) { reset(); return; }
                if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    phase = Phase.RETURN;
                    stallTicks = 0;
                } else if (++stallTicks > MAX_STALL) {
                    reset();
                }
                break;
            }

            case RETURN: {
                if (!validHandler(mc)) { reset(); return; }
                if (!mc.player.inventory.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                    phase = Phase.WAIT_RETURN;
                    stallTicks = 0;
                } else {
                    phase = Phase.COOLDOWN;
                    waitTicks = 2;
                }
                break;
            }

            case WAIT_RETURN: {
                if (!validHandler(mc)) { reset(); return; }
                if (mc.player.inventory.getCursorStack().isEmpty()) {
                    phase = Phase.COOLDOWN;
                    waitTicks = 2;
                    stallTicks = 0;
                } else if (++stallTicks > MAX_STALL) {
                    // Force drop attempt / abort
                    reset();
                }
                break;
            }

            case COOLDOWN:
                reset();
                break;
        }
    }

    private boolean slotIsTotem(MinecraftClient mc, int invIndex) {
        if (invIndex < 0 || invIndex >= 36) return false;
        return mc.player.inventory.getStack(invIndex).getItem() == Items.TOTEM_OF_UNDYING;
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
