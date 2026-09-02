package com.createnuclearindustrys.Blocks.ThermalGeneratorBlock;

import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeBlockEntity;
import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.Utills.Interfaces.HeatNodeBlock;
import com.createnuclearindustrys.Utills.Interfaces.Heat_syncer;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

import java.util.Map;

public class ThermalGeneratorBlock extends DirectionalKineticBlock implements IBE<ThermalGeneratorBlockEntity>, HeatNodeBlock, Heat_syncer {

    public ThermalGeneratorBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = this.getPreferredFacing(context);
        return (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) && preferred != null
                ? this.defaultBlockState().setValue(FACING, preferred)
                : super.getStateForPlacement(context);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public boolean hideStressImpact() {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public Class<ThermalGeneratorBlockEntity> getBlockEntityClass() {
        return ThermalGeneratorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThermalGeneratorBlockEntity> getBlockEntityType() {
        return CNIBlocks.THERMAL_GENERATOR_BLOCK_ENTITY.get();
    }
    public void heat_sync(Map.Entry<BlockPos, Float> entry, ServerLevel level, float MAX_TEMP) {
        if (level.getBlockEntity(entry.getKey()) instanceof ThermalGeneratorBlockEntity tgbe){
            tgbe.setHeat(entry.getValue());
        }
    }
}