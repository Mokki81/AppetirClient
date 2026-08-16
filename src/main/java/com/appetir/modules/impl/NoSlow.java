package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Module {

    private final BooleanSetting items = new BooleanSetting("Items", "No slow while using items", true);
    private final BooleanSetting soulSand = new BooleanSetting("SoulSand", "No soul sand slow", true);

    public NoSlow() {
        super("NoSlow", "Убирает замедление", Category.MOVEMENT);
        addSetting(items);
        addSetting(soulSand);
    }

    public boolean items() { return isEnabled() && items.get(); }
    public boolean soulSand() { return isEnabled() && soulSand.get(); }

    @Override
    public void onTick() {
        if (!soulSand()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        BlockPos below = new BlockPos(mc.player.getX(), mc.player.getY() - 0.2, mc.player.getZ());
        if (mc.world.getBlockState(below).getBlock() != Blocks.SOUL_SAND
                && mc.world.getBlockState(below).getBlock() != Blocks.SOUL_SOIL) {
            return;
        }

        // Compensate soul-sand friction by restoring some horizontal speed toward input
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        if (forward == 0 && strafe == 0) return;

        double yaw = Math.toRadians(mc.player.yaw);
        double mx = forward * -Math.sin(yaw) + strafe * Math.cos(yaw);
        double mz = forward * Math.cos(yaw) + strafe * Math.sin(yaw);
        double len = Math.sqrt(mx * mx + mz * mz);
        if (len < 1e-6) return;
        mx /= len;
        mz /= len;

        Vec3d vel = mc.player.getVelocity();
        double boost = 0.08;
        mc.player.setVelocity(vel.x + mx * boost, vel.y, vel.z + mz * boost);
    }
}
