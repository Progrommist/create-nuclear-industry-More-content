package com.createnuclearindustrys.Blocks.PlutoniumFuelRod;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.Utils.Interfaces.Emitted.FuelRodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class PlutoniumFuelRodEntity extends BlockEntity implements FuelRodBlockEntity {
    private int durability = 200000;
    public PlutoniumFuelRodEntity(BlockPos pos, BlockState blockState) {
        super(CNIBlocks.PLUTONIUM_FUEL_ROD_ENTITY.get(), pos, blockState);
    }

    public int getDurability() {
        return this.durability;
    }
    public void decDurability() {
        durability--;
        if (this.durability <= 0) {
            level.setBlock(worldPosition, CNIBlocks.ZINC_ROD.get().defaultBlockState(), 3);
        }
        this.setChanged();
    }
    public void setDurability(int Value) {
        durability = Value;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("durability", this.durability);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.durability = tag.getInt("durability");
    }
}
