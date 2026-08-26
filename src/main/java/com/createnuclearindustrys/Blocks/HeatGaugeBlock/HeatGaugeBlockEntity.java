package com.createnuclearindustrys.Blocks.HeatGaugeBlock;

import com.createnuclearindustrys.CNIBlocks;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class HeatGaugeBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    float heat = 0f;

    public HeatGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(CNIBlocks.HEAT_GAUGE_BLOCK_ENTITY.get(), pos, state);
    }

    public void setHeat(float newHeat) {
        if (Math.abs(heat - newHeat) < 0.1f) return;

        // Detect if the redstone signal strength (0–15) would change so we only
        // notify neighbors when the value actually crosses a step boundary.
        int oldSignal = Math.min(15, (int)(heat    / 1000f * 15));
        int newSignal = Math.min(15, (int)(newHeat / 1000f * 15));

        heat = newHeat;
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

            // Push a neighbor update so comparators and redstone wires see the new signal
            if (oldSignal != newSignal)
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("    Heat Gauge").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("Temp:  " + String.format("%.1f°C", heat)).withStyle(ChatFormatting.GOLD));
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("heat", heat);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heat = tag.getFloat("heat");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
