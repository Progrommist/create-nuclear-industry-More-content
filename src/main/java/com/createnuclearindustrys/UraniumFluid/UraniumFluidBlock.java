package com.createnuclearindustrys.UraniumFluid;

import com.createnuclearindustrys.CNIFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class UraniumFluidBlock extends LiquidBlock {

    private static final int PARTICLES_PER_BLOCK = 40;
    public UraniumFluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        level.scheduleTick(pos, this, 1);
    }

    /** Replace the block with air and spray rising steam particles. */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        for (int i = 0; i < PARTICLES_PER_BLOCK; i++) {
            double ox = (rng.nextDouble() - 0.5) * 0.8;
            double oz = (rng.nextDouble() - 0.5) * 0.8;
            double oy = rng.nextDouble() * 0.3;

            // Horizontal spread, strong upward velocity
            double vx = (rng.nextDouble() - 0.5) * 0.06;
            double vy = 0.08 + rng.nextDouble() * 0.06;
            double vz = (rng.nextDouble() - 0.5) * 0.06;

            level.sendParticles(
                    CNIFluids.STEAM_PARTICLE.get(),
                    cx + ox, cy + oy, cz + oz,
                    1,
                    vx, vy, vz,
                    0.0
            );
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
