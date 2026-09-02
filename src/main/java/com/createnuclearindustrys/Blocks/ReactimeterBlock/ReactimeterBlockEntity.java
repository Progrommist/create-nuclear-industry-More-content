package com.createnuclearindustrys.Blocks.ReactimeterBlock;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.Config;
import com.createnuclearindustrys.CreateNuclearIndustrys;
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

public class ReactimeterBlockEntity extends BlockEntity implements IHaveGoggleInformation {
    float reactivity = 0f;
    private float lastHeat = 0f;

    public ReactimeterBlockEntity(BlockPos pos, BlockState state) {
        super(CNIBlocks.REACTIMETER_BLOCK_ENTITY.get(), pos, state);
    }

    public void setHeat(float newHeat) {
        if (Math.abs(lastHeat - newHeat) < 0.1f) return;

        float oldReactivity = reactivity;

        float dT = newHeat - lastHeat;
        reactivity = dT / Config.MAX_TEMPERATURE.get(); // I have no idea how to cache config here
        //CreateNuclearIndustrys.LOGGER.info(String.valueOf(reactivity));

        // Detect if the redstone signal strength (0–15) would change so we only
        // notify neighbors when the value actually crosses a step boundary.
        int oldSignal = Math.min(15, (int)((oldReactivity + 1) * 8));
        int newSignal = Math.min(15, (int)((reactivity + 1) * 8));

        lastHeat = newHeat;
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
        tooltip.add(Component.literal("    Reactimeter").withStyle(ChatFormatting.WHITE));
        if (reactivity < 0.001f) tooltip.add(Component.literal("Reactivity:  " + String.format("%.3f", reactivity)).withStyle(ChatFormatting.GREEN));
        else if (reactivity < 0.004f) tooltip.add(Component.literal("Reactivity:  " + String.format("%.3f", reactivity)).withStyle(ChatFormatting.YELLOW));
        else tooltip.add(Component.literal("Reactivity:  " + String.format("%.3f", reactivity)).withStyle(ChatFormatting.RED));
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("reactivity", reactivity);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        reactivity = tag.getFloat("reactivity");
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
