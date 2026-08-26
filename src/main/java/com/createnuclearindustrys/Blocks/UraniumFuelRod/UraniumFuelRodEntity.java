package com.createnuclearindustrys.Blocks.UraniumFuelRod;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.Config;
import com.createnuclearindustrys.Manament.RadiationManager;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class UraniumFuelRodEntity extends BlockEntity{
    private int durability = 100000;
    public UraniumFuelRodEntity(BlockPos pos, BlockState blockState) {
        super(CNIBlocks.URANIUM_FUEL_ROD_ENTITY.get(), pos, blockState);
    }

    public int getDurability() {
        return durability;
    }

    public void decDurability() {
        this.durability--;
        if (this.durability <= 0) {
            level.setBlock(worldPosition, CNIBlocks.ZINC_ROD.get().defaultBlockState(), 3);
        }
        this.setChanged();
    }
    public void setDurability(int Value) {
        this.durability = Value;
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
