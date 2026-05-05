package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

// BlockESP подсвечивает интересные блоки (сундуки, спавнеры).
// Рендер через mixin на WorldRenderer::render.
public class BlockESP extends Module {

    public static final List<BlockPos> interestingBlocks = new ArrayList<>();

    public BlockESP() {
        super("BlockESP", "Подсветка блоков (сундуки, спавнеры и т.д.)", Category.RENDER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        interestingBlocks.clear();
        BlockPos center = mc.player.getBlockPos();
        int range = 32;

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = center.add(x, y, z);
                    var block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.CHEST ||
                        block == Blocks.TRAPPED_CHEST ||
                        block == Blocks.ENDER_CHEST ||
                        block == Blocks.SPAWNER ||
                        block == Blocks.BARREL) {
                        interestingBlocks.add(pos);
                    }
                }
            }
        }
    }
}
