package com.createnuclearindustrys.Blocks.BoilerBlock;

import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeBlockEntity;
import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.CreateNuclearIndustrys;
import com.createnuclearindustrys.Manament.RadiationManager;
import com.createnuclearindustrys.Utills.Interfaces.HeatNodeBlock;
import com.createnuclearindustrys.Utills.Interfaces.Heat_syncer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Map;

public class BoilerBlock extends Block implements EntityBlock, HeatNodeBlock, Heat_syncer {

    public BoilerBlock(Properties properties) {
        super(properties);
    }

    /**
     * Fires whenever this block appears here by any means — pistons, Create contraptions,
     * falling blocks, /setblock — so a moved boiler stays on the heat network.
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (oldState.is(this) || !(level instanceof ServerLevel serverLevel)) return;
        RadiationManager.get(serverLevel).registerMovedNode(serverLevel, pos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoilerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != CNIBlocks.BOILER_BLOCK_ENTITY.get()) return null;
        return (lvl, pos, blockState, be) -> ((BoilerBlockEntity) be).tick();
    }
    @Override
    public void heat_sync(Map.Entry<BlockPos, Float> entry, ServerLevel level, float MAX_TEMP) {
        if (level.getBlockEntity(entry.getKey()) instanceof BoilerBlockEntity be){
            be.setHeat(entry.getValue());
        }
    }
}
