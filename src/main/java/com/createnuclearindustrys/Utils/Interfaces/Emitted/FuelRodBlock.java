package com.createnuclearindustrys.Utils.Interfaces.Emitted;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public interface FuelRodBlock {
    public static final IntegerProperty HEAT_LEVEL = IntegerProperty.create("heat_level", 0, 15);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
}
