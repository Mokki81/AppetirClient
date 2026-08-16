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

    private final NumberSetting range = new NumberSetting("Range", "Horizontal scan range", 24, 8, 48, 4);
    private final NumberSetting yRange = new NumberSetting("YRange", "Vertical scan range", 16, 4, 32, 2);
    private final NumberSetting interval = new NumberSetting("Interval", "Scan every N ticks", 40, 10, 100, 5);
    private final BooleanSetting chests = new BooleanSetting("Chests", "Chests / barrels", true);
    private final BooleanSetting spawners = new BooleanSetting("Spawners", "Mob spawners", true);
    private final BooleanSetting shulkers = new BooleanSetting("Shulkers", "Shulker boxes", true);
    private final BooleanSetting portals = new BooleanSetting("Portals", "Nether portals / end portal", false);

    private int tickCounter = 0;
    private int scanX, scanY, scanZ;
    private boolean scanning;
    private BlockPos scanCenter;
    private final List<BlockPos> scanBuffer = new ArrayList<>();

    public BlockESP() {
        super("BlockESP", "Подсветка блоков", Category.RENDER);
        addSetting(range);
        addSetting(yRange);
        addSetting(interval);
        addSetting(chests);
        addSetting(spawners);
        addSetting(shulkers);
        addSetting(portals);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (scanning) {
            continueScan(mc);
            return;
        }

        if (++tickCounter < interval.getInt()) return;
        tickCounter = 0;
        startScan(mc);
    }

    @Override
    public void onDisable() {
        interestingBlocks.clear();
        scanBuffer.clear();
        scanning = false;
    }

    private void startScan(MinecraftClient mc) {
        scanCenter = mc.player.getBlockPos();
        scanX = -range.getInt();
        scanY = -yRange.getInt();
        scanZ = -range.getInt();
        scanBuffer.clear();
        scanning = true;
    }

    /** Incremental: process limited blocks per tick to avoid freezes. */
    private void continueScan(MinecraftClient mc) {
        int r = range.getInt();
        int yr = yRange.getInt();
        int budget = 8000; // blocks per tick

        while (budget-- > 0) {
            if (scanX > r) {
                // done
                interestingBlocks.clear();
                interestingBlocks.addAll(scanBuffer);
                scanning = false;
                return;
            }

            BlockPos pos = scanCenter.add(scanX, scanY, scanZ);
            if (mc.world.isChunkLoaded(pos)) {
                Block block = mc.world.getBlockState(pos).getBlock();
                if (isInteresting(block)) {
                    scanBuffer.add(pos.toImmutable());
                }
            }

            scanZ++;
            if (scanZ > r) {
                scanZ = -r;
                scanY++;
                if (scanY > yr) {
                    scanY = -yr;
                    scanX++;
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
