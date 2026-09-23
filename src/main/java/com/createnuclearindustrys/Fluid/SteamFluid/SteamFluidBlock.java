package com.createnuclearindustrys.Fluid.SteamFluid;

import com.createnuclearindustrys.CNIFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Steam fluid block — instantly dissipates into rising steam particles
 * one tick after being placed in the world.
 */
public class SteamFluidBlock extends LiquidBlock {

    private static final int PARTICLES_PER_BLOCK = 25;
    // Widest cone slope: blocks outward per block risen (0.35 → ~3.5 blocks wide at 10 blocks up)
    private static final double MAX_SPREAD_SLOPE = 0.35;

    public SteamFluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    /** Schedule the dissipation tick the moment steam is placed. */
    @Override
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

            // Random outward direction and slope; the particle spreads by this much per block it rises,
            // so the plume forms a cone. sqrt fills the cone evenly instead of bunching at the center.
            double angle = rng.nextDouble() * Math.PI * 2;
            double slope = Math.sqrt(rng.nextDouble()) * MAX_SPREAD_SLOPE;
            double vx = Math.cos(angle) * slope;
            double vz = Math.sin(angle) * slope;

            // count 0 makes the client pass (vx, 0, vz) × 1.0 straight to the particle;
            // with count 1 it would treat them as position jitter and drop them
            level.sendParticles(
                    CNIFluids.STEAM_PARTICLE.get(),
                    cx + ox, cy + oy, cz + oz,
                    0,
                    vx, 0.0, vz,
                    1.0
            );
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
