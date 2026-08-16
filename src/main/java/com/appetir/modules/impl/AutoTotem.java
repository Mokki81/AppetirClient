package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {

    private enum Phase {
        IDLE, WAIT_PICKUP, WAIT_OFFHAND, WAIT_RETURN, RECOVERY, COOLDOWN
    }

    private Phase phase = Phase.IDLE;
    private int containerSlot = -1;
    private int syncId = -1;
    private int timeout = 0;
    private ItemStack previousOffhand = ItemStack.EMPTY;

    public AutoTotem() {
        super("AutoTotem", "Автоматически кладёт тотем в оффхенд", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        // Try one safe recovery if cursor still held
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && !mc.player.inventory.getCursorStack().isEmpty()) {
            tryRecover(mc);
        }
        hardReset();
    }

    private void hardReset() {
        phase = Phase.IDLE;
        containerSlot = -1;
        syncId = -1;
        timeout = 0;
        previousOffhand = ItemStack.EMPTY;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null || mc.world == null) {
            hardReset();
            return;
        }

        if (!(mc.player.currentScreenHandler instanceof PlayerScreenHandler)
                || mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
            hardReset();
            return;
        }

        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING
                && mc.player.inventory.getCursorStack().isEmpty()) {
            hardReset();
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
                mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                phase = Phase.WAIT_PICKUP;
                timeout = 12;
                break;
            }
            case WAIT_PICKUP: {
                if (!validHandler(mc) || --timeout <= 0) {
                    enterRecovery(mc);
                    return;
                }
                if (mc.player.inventory.getCursorStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                    phase = Phase.WAIT_OFFHAND;
                    timeout = 12;
                }
                break;
            }
            case WAIT_OFFHAND: {
                if (!validHandler(mc) || --timeout <= 0) {
                    enterRecovery(mc);
                    return;
                }
                if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    ItemStack cursor = mc.player.inventory.getCursorStack();
                    if (cursor.isEmpty()) {
                        phase = Phase.COOLDOWN;
                        timeout = 3;
                    } else {
                        // Prefer returning previous offhand to empty source
                        if (isSourceEmpty(mc, containerSlot)
                                || itemMatches(cursor, previousOffhand)) {
                            mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                            phase = Phase.WAIT_RETURN;
                            timeout = 12;
                        } else {
                            enterRecovery(mc);
                        }
                    }
                }
                break;
            }
            case WAIT_RETURN: {
                if (!validHandler(mc) || --timeout <= 0) {
                    enterRecovery(mc);
                    return;
                }
                if (mc.player.inventory.getCursorStack().isEmpty()) {
                    phase = Phase.COOLDOWN;
                    timeout = 3;
                }
                break;
            }
            case RECOVERY: {
                if (!validHandler(mc) || --timeout <= 0) {
                    // Give up — leave cursor rather than wrong swap
                    phase = Phase.COOLDOWN;
                    timeout = 5;
                    return;
                }
                if (mc.player.inventory.getCursorStack().isEmpty()) {
                    phase = Phase.COOLDOWN;
                    timeout = 3;
                    return;
                }
                tryRecover(mc);
                break;
            }
            case COOLDOWN: {
                if (--timeout <= 0) hardReset();
                break;
            }
        }
    }

    private void enterRecovery(MinecraftClient mc) {
        phase = Phase.RECOVERY;
        timeout = 8;
        tryRecover(mc);
    }

    /**
     * Only put cursor back if source is empty OR cursor matches previousOffhand and source empty.
     */
    private void tryRecover(MinecraftClient mc) {
        if (!validHandler(mc)) return;
        ItemStack cursor = mc.player.inventory.getCursorStack();
        if (cursor.isEmpty()) return;

        if (containerSlot >= 0 && isSourceEmpty(mc, containerSlot)) {
            mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
        }
        // else leave cursor — do not blind-swap into occupied slot
    }

    private boolean itemMatches(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        return a.getItem() == b.getItem();
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
            if (mc.player.inventory.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) return i;
        }
        return -1;
    }
}
