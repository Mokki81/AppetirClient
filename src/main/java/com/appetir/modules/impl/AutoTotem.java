package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Moves a totem into offhand with full 3-click swap so cursor is never left holding an item.
 */
public class AutoTotem extends Module {

    private int cooldown = 0;

    public AutoTotem() {
        super("AutoTotem", "Автоматически кладёт тотем в оффхенд", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null || mc.world == null) return;
        if (cooldown > 0) { cooldown--; return; }

        // Only while player inventory is the active handler (not chest/etc.)
        if (!(mc.player.currentScreenHandler instanceof PlayerScreenHandler)) return;
        if (mc.player.currentScreenHandler != mc.player.playerScreenHandler) return;

        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
        // Don't swap while cursor already holds something — wait
        if (!mc.player.inventory.getCursorStack().isEmpty()) return;

        int invSlot = findTotemInvSlot(mc);
        if (invSlot < 0) return;

        int containerSlot = invSlot < 9 ? invSlot + 36 : invSlot;
        int syncId = mc.player.playerScreenHandler.syncId;

        // 1) pickup totem
        mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
        // 2) place into offhand (45) — if offhand had item, it goes to cursor
        mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
        // 3) put previous offhand item (now on cursor) back into source slot
        mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);

        cooldown = 2; // brief pause to avoid double-fire
    }

    private int findTotemInvSlot(MinecraftClient mc) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) return i;
        }
        return -1;
    }
}
