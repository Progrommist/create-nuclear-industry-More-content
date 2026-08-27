package com.createnuclearindustrys.Blocks.ThermalGeneratorBlock;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.CNIFluids;
import com.createnuclearindustrys.CNITriggers;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.List;

public class ThermalGeneratorBlockEntity extends GeneratingKineticBlockEntity {

    private static final float MAX_SU = 2048f;

    // Fluid tank sizes
    private static final int WATER_CAPACITY = 4000;   // 4 buckets of water buffer
    private static final int STEAM_CAPACITY  = 8000;  // 8 buckets of steam output buffer

    // Per-tick conversion rate when running: 5 mB water → 50 mB steam (10× expansion)
    private static final int WATER_PER_TICK = 5;
    private static final int STEAM_PER_TICK  = 50;

    float heat = 0f;
    /** Tracks water state so we can notify the kinetic network if it changes between heat updates. */
    private boolean hadWaterLastTick = false;

    /** Input tank — accepts only water. */
    private final FluidTank waterTank = new FluidTank(WATER_CAPACITY,
            stack -> stack.is(Fluids.WATER));

    /** Output tank — holds produced steam for pipes to drain. */
    private final FluidTank steamTank = new FluidTank(STEAM_CAPACITY);

    /**
     * Combined IFluidHandler exposed to Create pipes / other mods:
     *   tank 0 = water input  (fill-only from outside)
     *   tank 1 = steam output (drain-only from outside)
     */
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
            // Only water can be put into tank 0; steam tank is output-only
            return tank == 0 && waterTank.isFluidValid(stack);
        }

        /** External fill → goes into the water input tank. */
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = waterTank.fill(resource, action);
            if (filled > 0 && action.execute()) setChanged();
            return filled;
        }

        /** External drain → pulls from the steam output tank. */
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

    public ThermalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(CNIBlocks.THERMAL_GENERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // no extra Create behaviours needed
    }

    // ── Kinetic output ────────────────────────────────────────────────────────

    /** Only generate rotation when both hot AND supplied with water. */
    @Override
    public float getGeneratedSpeed() {
        //return (heat > 10f && hasWater() && !fullSteam()) ? 16f : 0f;
        if (heat < 10f || !hasWater() || fullSteam()) return 0;

        return convertToDirection(16.0f, (Direction)this.getBlockState().getValue(CreativeMotorBlock.FACING));
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!hasWater()) return 0f;
        if (fullSteam()) return 0f;
        return Math.max(0f, (heat / 1000f) * MAX_SU);
    }

    // ── Fluid API ─────────────────────────────────────────────────────────────

    /** Returns the IFluidHandler exposed to Create pipes and capability queries. */
    public IFluidHandler getFluidHandler() {
        return combinedHandler;
    }

    /** True if the water tank has any water left to convert. */
    public boolean hasWater() {
        return !waterTank.isEmpty();
    }
    public boolean fullSteam() {
        return steamTank.getSpace() == 0;
    }
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

    // ── Per-tick conversion logic ─────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;

        boolean hasWaterNow = hasWater();

        // While running: consume water and produce steam each tick
        if (heat > 10f && hasWaterNow) {
            FluidStack consumed = waterTank.drain(WATER_PER_TICK, IFluidHandler.FluidAction.EXECUTE);
            if (!consumed.isEmpty()) {
                steamTank.fill(
                        new FluidStack(CNIFluids.STEAM_STILL.get(),
                                consumed.getAmount() * 2),   // 2× expansion: 5 mB water → 10 mB steam
                        IFluidHandler.FluidAction.EXECUTE);
                setChanged();
            }
        }

        // If water availability changed (ran dry or was refilled), update the kinetic network
        if (hasWaterNow != hadWaterLastTick) {
            hadWaterLastTick = hasWaterNow;
            updateGeneratedRotation();
            if (hasNetwork()) notifyStressCapacityChange(calculateAddedStressCapacity());
            Level level = this.getLevel();
            if (level instanceof ServerLevel serverLevel) {
                CNITriggers.THERMAL_GENERATOR_TRIGGER.get().trigger(findClosestPlayer(this.getBlockPos().getCenter(), serverLevel));
            }
        }
    }

    // ── Heat update (called by RadiationManager) ──────────────────────────────

    public void setHeat(float newHeat) {
        if (Math.abs(heat - newHeat) < 0.5f) return;
        boolean wasRunning = heat > 10f && hasWater();
        heat = newHeat;
        boolean nowRunning = heat > 10f && hasWater();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            updateGeneratedRotation();
            if (wasRunning != nowRunning && hasNetwork())
                notifyStressCapacityChange(calculateAddedStressCapacity());
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("heat", heat);
        tag.put("waterTank", waterTank.writeToNBT(registries, new CompoundTag()));
        tag.put("steamTank",  steamTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        heat = tag.getFloat("heat");
        if (tag.contains("waterTank")) waterTank.readFromNBT(registries, tag.getCompound("waterTank"));
        if (tag.contains("steamTank"))  steamTank.readFromNBT(registries,  tag.getCompound("steamTank"));
    }
    @Nullable
    public ServerPlayer findClosestPlayer(Vec3 position, ServerLevel server) {
        List<ServerPlayer> players = server.players();
        ServerPlayer closest = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (ServerPlayer player : players) {
            double distanceSq = player.position().distanceToSqr(position);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closest = player;
            }
        }
        return closest;
    }
}
