package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    public AutoTotem() { super("AutoTotem","Автоматическое использование тотемов",Category.COMBAT); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null||mc.interactionManager==null) return;
        if (mc.player.getOffHandStack().getItem()==Items.TOTEM_OF_UNDYING) return;

        for (int i=0;i<mc.player.inventory.size();i++) {
            if (mc.player.inventory.getStack(i).getItem()==Items.TOTEM_OF_UNDYING) {
                int slot = i<9?i+36:i;
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,slot,0,SlotActionType.PICKUP,mc.player);
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,45,0,SlotActionType.PICKUP,mc.player);
                return;
            }
        }
    }
}
