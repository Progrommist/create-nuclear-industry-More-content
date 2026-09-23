package com.createnuclearindustrys.Blocks.BoilerBlock;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.CNIFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class BoilerBlockEntity extends BlockEntity {
    private static final int WATER_CAPACITY = 4000;
    private static final int STEAM_CAPACITY  = 8000;
    private int syncedWaterAmount;
    private int syncedSteamAmount;
    private static final float MIN_HEAT = 100f;

    float heat = 0f;
    // Fraction of a mB of water carried between ticks so the boil rate follows heat exactly
    private float waterOwed = 0f;
    // Whether water actually boiled this tick; the heat network only cools a boiler that is boiling
    private boolean boiling = false;

    private final FluidTank waterTank = new FluidTank(WATER_CAPACITY, stack -> stack.is(Fluids.WATER));
    private final FluidTank steamTank = new FluidTank(STEAM_CAPACITY);

    private final IFluidHandler combinedHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? waterTank.getFluid() : steamTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? waterTank.getCapacity() : steamTank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && waterTank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = waterTank.fill(resource, action);
            if (filled > 0 && action.execute()) setChanged();
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack drained = steamTank.drain(resource, action);
            if (!drained.isEmpty() && action.execute()) setChanged();
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack drained = steamTank.drain(maxDrain, action);
            if (!drained.isEmpty() && action.execute()) setChanged();
            return drained;
        }
    };
    public int waterAmount() {
        return waterTank.getFluidAmount();
    }
    public int steamAmount() {
        return steamTank.getFluidAmount();
    }
    public int waterCapacity() {
        return waterTank.getCapacity();
    }
    public int steamCapacity() {
        return steamTank.getCapacity();
    }
    public float getHeat() {
        return heat;
    }

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        super(CNIBlocks.BOILER_BLOCK_ENTITY.get(), pos, state);
    }

    public void tick() {
        boiling = false;

        if (Math.abs(waterTank.getFluidAmount() - syncedWaterAmount) >= WATER_CAPACITY / 100 ||
                Math.abs(steamTank.getFluidAmount() - syncedSteamAmount) >= STEAM_CAPACITY / 100) {
            if (level instanceof ServerLevel sl) {
                sl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
                syncedWaterAmount = waterTank.getFluidAmount();
                syncedSteamAmount = steamTank.getFluidAmount();
            }
        }

        if (level == null || level.isClientSide() || heat < MIN_HEAT || waterTank.isEmpty()) return;

        // Scale linearly: 1 mB water/tick per 100°C, fractions carried over (287°C averages 2.87 mB/tick)
        waterOwed += heat / 100f;
        // Only boil what the steam tank has room for — a backed-up boiler stops instead of
        // destroying the water whose steam wouldn't fit
        int room = (STEAM_CAPACITY - steamTank.getFluidAmount()) / 10;
        int waterPerTick = Math.min((int) waterOwed, room);
        if (waterPerTick <= 0) {
            waterOwed = Math.min(waterOwed, 1f);
            return;
        }
        waterOwed -= waterPerTick;
        FluidStack consumed = waterTank.drain(waterPerTick, IFluidHandler.FluidAction.EXECUTE);
        if (!consumed.isEmpty()) {
            steamTank.fill(
                    new FluidStack(CNIFluids.STEAM_STILL.get(), consumed.getAmount() * 10),
                    IFluidHandler.FluidAction.EXECUTE
            );
            boiling = true;
            setChanged();
        }
    }

    public void setHeat(float newHeat) {
        if (Math.abs(heat - newHeat) < 0.5f) return;
        heat = newHeat;
        setChanged();
    }
    public boolean hasWater() { return !waterTank.isEmpty(); }

    public boolean isBoiling() { return boiling; }

    public IFluidHandler getFluidHandler() { return combinedHandler; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("heat", heat);
        tag.put("waterTank", waterTank.writeToNBT(registries, new CompoundTag()));
        tag.put("steamTank",  steamTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heat = tag.getFloat("heat");
        if (tag.contains("waterTank")) waterTank.readFromNBT(registries, tag.getCompound("waterTank"));
        if (tag.contains("steamTank"))  steamTank.readFromNBT(registries,  tag.getCompound("steamTank"));
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