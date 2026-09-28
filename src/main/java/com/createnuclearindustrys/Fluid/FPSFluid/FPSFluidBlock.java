package com.createnuclearindustrys.Fluid.FPSFluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class FPSFluidBlock extends LiquidBlock {
    public FPSFluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        level.scheduleTick(pos, this, 1);
    }
}
