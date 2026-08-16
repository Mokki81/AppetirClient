package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * State machine: advance only when client inventory reflects expected state.
 */
public class AutoTotem extends Module {

    private enum Phase {
        IDLE, WAIT_PICKUP, WAIT_OFFHAND, WAIT_RETURN, COOLDOWN
    }

    private Phase phase = Phase.IDLE;
    private int containerSlot = -1;
    private int syncId = -1;
    private int timeout = 0;

    public AutoTotem() {
        super("AutoTotem", "Автоматически кладёт тотем в оффхенд", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        phase = Phase.IDLE;
        containerSlot = -1;
        syncId = -1;
        timeout = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null || mc.world == null) {
            phase = Phase.IDLE;
            return;
        }

        if (!(mc.player.currentScreenHandler instanceof PlayerScreenHandler)
                || mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
            phase = Phase.IDLE;
            return;
        }

        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            phase = Phase.IDLE;
            return;
        }

        switch (phase) {
            case IDLE: {
                if (!mc.player.inventory.getCursorStack().isEmpty()) return;
                int inv = findTotemInvSlot(mc);
                if (inv < 0) return;
                containerSlot = inv < 9 ? inv + 36 : inv;
                syncId = mc.player.playerScreenHandler.syncId;
                // Verify slot still has totem
                if (!slotHasTotem(mc, containerSlot)) return;
                mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                phase = Phase.WAIT_PICKUP;
                timeout = 10;
                break;
            }
            case WAIT_PICKUP: {
                if (!validHandler(mc) || --timeout <= 0) {
                    abort(mc);
                    return;
                }
                if (cursorIsTotem(mc)) {
                    mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                    phase = Phase.WAIT_OFFHAND;
                    timeout = 10;
                }
                break;
            }
            case WAIT_OFFHAND: {
                if (!validHandler(mc) || --timeout <= 0) {
                    abort(mc);
                    return;
                }
                if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    // Return whatever is on cursor (previous offhand)
                    if (!mc.player.inventory.getCursorStack().isEmpty()) {
                        mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                        phase = Phase.WAIT_RETURN;
                        timeout = 10;
                    } else {
                        phase = Phase.COOLDOWN;
                        timeout = 3;
                    }
                }
                break;
            }
            case WAIT_RETURN: {
                if (!validHandler(mc) || --timeout <= 0) {
                    // Best effort: try put cursor anywhere safe
                    abort(mc);
                    return;
                }
                if (mc.player.inventory.getCursorStack().isEmpty()) {
                    phase = Phase.COOLDOWN;
                    timeout = 3;
                }
                break;
            }
            case COOLDOWN: {
                if (--timeout <= 0) {
                    phase = Phase.IDLE;
                    containerSlot = -1;
                }
                break;
            }
        }
    }

    private void abort(MinecraftClient mc) {
        // If cursor holds something, try return to source slot once
        try {
            if (validHandler(mc) && !mc.player.inventory.getCursorStack().isEmpty() && containerSlot >= 0) {
                mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
            }
        } catch (Exception ignored) {}
        phase = Phase.COOLDOWN;
        timeout = 5;
    }

    private boolean validHandler(MinecraftClient mc) {
        return mc.player != null
                && mc.interactionManager != null
                && mc.player.currentScreenHandler instanceof PlayerScreenHandler
                && mc.player.currentScreenHandler.syncId == syncId;
    }

    private boolean cursorIsTotem(MinecraftClient mc) {
        return mc.player.inventory.getCursorStack().getItem() == Items.TOTEM_OF_UNDYING;
    }

    private boolean slotHasTotem(MinecraftClient mc, int containerSlot) {
        // Map container slot back to inventory index for read
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
