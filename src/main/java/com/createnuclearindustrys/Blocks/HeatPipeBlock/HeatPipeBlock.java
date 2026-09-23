package com.createnuclearindustrys.Blocks.HeatPipeBlock;

import com.createnuclearindustrys.Manament.RadiationManager;
import com.createnuclearindustrys.Utills.Interfaces.HeatNodeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HeatPipeBlock extends Block implements HeatNodeBlock {
    public HeatPipeBlock(Properties properties) {
        super(properties);
    }

    /**
     * Fires whenever this block appears here by any means — pistons, Create contraptions,
     * falling blocks, /setblock — so a moved pipe stays on the heat network.
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (oldState.is(this) || !(level instanceof ServerLevel serverLevel)) return;
        RadiationManager.get(serverLevel).registerMovedNode(serverLevel, pos);
    }
}
