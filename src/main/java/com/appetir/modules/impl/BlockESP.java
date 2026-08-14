package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BlockESP extends Module {

    public static final List<BlockPos> interestingBlocks = new ArrayList<>();

    private final NumberSetting range = new NumberSetting("Range", "Scan range", 32, 8, 64, 4);
    private final NumberSetting interval = new NumberSetting("Interval", "Scan every N ticks", 20, 5, 60, 5);
    private final BooleanSetting chests = new BooleanSetting("Chests", "Chests / barrels", true);
    private final BooleanSetting spawners = new BooleanSetting("Spawners", "Mob spawners", true);
    private final BooleanSetting shulkers = new BooleanSetting("Shulkers", "Shulker boxes", true);
    private final BooleanSetting portals = new BooleanSetting("Portals", "Nether portals / end portal", false);

    private int tickCounter = 0;

    public BlockESP() {
        super("BlockESP", "Подсветка блоков", Category.RENDER);
        addSetting(range);
        addSetting(interval);
        addSetting(chests);
        addSetting(spawners);
        addSetting(shulkers);
        addSetting(portals);
    }

    @Override
    public void onTick() {
        if (++tickCounter < interval.getInt()) return;
        tickCounter = 0;
        scan();
    }

    @Override
    public void onDisable() {
        interestingBlocks.clear();
    }

    private void scan() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        interestingBlocks.clear();
        BlockPos center = mc.player.getBlockPos();
        int r = range.getInt();

        // scan in steps of 1 — still ok for 32, skip air quickly
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (isInteresting(block)) {
                        interestingBlocks.add(pos.toImmutable());
                    }
                }
            }
        }
    }

    private boolean isInteresting(Block block) {
        if (chests.get()) {
            if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST
                    || block == Blocks.ENDER_CHEST || block == Blocks.BARREL)
                return true;
        }
        if (spawners.get() && block == Blocks.SPAWNER) return true;
        if (shulkers.get() && block == Blocks.SHULKER_BOX) return true;
        if (portals.get() && (block == Blocks.NETHER_PORTAL || block == Blocks.END_PORTAL
                || block == Blocks.END_PORTAL_FRAME))
            return true;
        return false;
    }
}
