package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;

public class MineHelper extends Module {

    public MineHelper() {
        super("MineHelper", "Помощник для майнинга", Category.MISC);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != net.minecraft.util.hit.HitResult.Type.BLOCK) return;

        var blockHit = (net.minecraft.util.hit.BlockHitResult) mc.crosshairTarget;
        BlockPos pos = blockHit.getBlockPos();
        Block block = mc.world.getBlockState(pos).getBlock();

        // Автоматически выбираем лучший инструмент
        int bestSlot = getBestToolSlot(mc, block);
        if (bestSlot != -1) mc.player.getInventory().selectedSlot = bestSlot;
    }

    private int getBestToolSlot(MinecraftClient mc, Block block) {
        float bestSpeed = -1;
        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            float speed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(
                ((net.minecraft.util.hit.BlockHitResult) mc.crosshairTarget).getBlockPos()));
            if (speed > bestSpeed) { bestSpeed = speed; bestSlot = i; }
        }
        return bestSlot;
    }
}
