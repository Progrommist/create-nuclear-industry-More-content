package com.createnuclearindustrys.Utills.Managment;

import com.createnuclearindustrys.Blocks.BoronControlRod.BoronControlRod;
import com.createnuclearindustrys.Blocks.HeatPipeBlock.HeatPipeBlock;
import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRod;
import com.createnuclearindustrys.Utills.Interfaces.HeatNodeBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class CommonInfo {
    public static boolean isHeatNode(Block b) {
        return b instanceof HeatNodeBlock;
    }
    @Nullable
    public static ServerPlayer findClosestPlayer(Vec3 position, ServerLevel server) {
        List<ServerPlayer> players = server.players();
        ServerPlayer closest = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (ServerPlayer player : players) {
            double distanceSq = player.position().distanceToSqr(position);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closest = player;
            }
        }
        return closest;
    }
    public static float getAbsorption(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof BoronControlRod) return 0.6f;
        if (b == Blocks.IRON_BLOCK || b instanceof UraniumFuelRod || b instanceof HeatPipeBlock) return 0f;
        if (b == Blocks.GOLD_BLOCK || b == Blocks.DIAMOND_BLOCK || b == Blocks.NETHERITE_BLOCK) return 0.05f;
        if (b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN) return 0.4f;
        if (b == Blocks.STONE || b == Blocks.COBBLESTONE || b == Blocks.DEEPSLATE
                || b == Blocks.STONE_BRICKS || b == Blocks.BRICKS || b == Blocks.SANDSTONE) return 0.15f;
        if (b == Blocks.SAND || b == Blocks.RED_SAND || b == Blocks.GRAVEL
                || b == Blocks.DIRT || b == Blocks.GRASS_BLOCK) return 0.25f;
        return 0.1f;
    }
}
