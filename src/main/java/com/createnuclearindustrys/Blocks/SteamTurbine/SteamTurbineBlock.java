package com.createnuclearindustrys.Blocks.SteamTurbine;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.CreateNuclearIndustrys;
import com.createnuclearindustrys.Utills.Interfaces.HeatNodeBlock;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SteamTurbineBlock extends DirectionalKineticBlock implements IBE<SteamTurbineBlockEntity>, HeatNodeBlock {

    public SteamTurbineBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getClickedFace());
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
    public Class<SteamTurbineBlockEntity> getBlockEntityClass() {
        return SteamTurbineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SteamTurbineBlockEntity> getBlockEntityType() {
        return CNIBlocks.STEAM_TURBINE_BLOCK_ENTITY.get();
    }
}
