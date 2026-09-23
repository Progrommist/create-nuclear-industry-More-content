package com.createnuclearindustrys.Blocks.HeatSourceBlock;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.Utills.Interfaces.HeatNodeBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CreativeHeatSourceBlock extends Block implements IBE<CreativeHeatSourceBlockEntity>, HeatNodeBlock {

    public CreativeHeatSourceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<CreativeHeatSourceBlockEntity> getBlockEntityClass() {
        return CreativeHeatSourceBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CreativeHeatSourceBlockEntity> getBlockEntityType() {
        return CNIBlocks.CREATIVE_HEAT_SOURCE_BLOCK_ENTITY.get();
    }
}
