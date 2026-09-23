package com.createnuclearindustrys.Blocks.SteamTurbine;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.CNIFluids;
import com.createnuclearindustrys.Config;
import com.createnuclearindustrys.CreateNuclearIndustrys;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

public class SteamTurbineBlockEntity extends GeneratingKineticBlockEntity {
    public static final float SPEED = 16f;
    // Stress capacity at full steam flow (MAX_THROUGHPUT), in SU as shown on a stressometer

    // Max steam processed per tick; actual throughput is pipe-limited
    private static final int MAX_THROUGHPUT = 100;
    // Fraction of steam consumed to generate power; the rest exits the output tank
    private static final float CONSUMPTION_RATIO = 0.10f;

    private static final int TANK_CAPACITY = 4000;

    // Steam flow is averaged over the last 100 ticks (5 s) so capacity follows heat smoothly
    // instead of jumping with pipe deliveries
    private static final int FLOW_WINDOW = 100;
    // Below this average flow (mB/tick) the turbine stops spinning
    private static final float MIN_FLOW = 0.5f;
    // Skip network updates for capacity changes smaller than this (per RPM; ×16 = 1.6 SU)
    private static final float CAPACITY_EPSILON = 0.1f;

    /** Steam fed in by pipes. */
    private final FluidTank steamInputTank = new FluidTank(TANK_CAPACITY,
            stack -> stack.getFluid() == CNIFluids.STEAM_STILL.get());

    /** Reduced steam pushed out to pipes. */
    private final FluidTank steamOutputTank = new FluidTank(TANK_CAPACITY);

    /**
     * Fill-only handler exposed on the back face (steam enters here).
     * Pipes see no fluid to drain, preventing them from treating the input as a source.
     */
    private final IFluidHandler inputOnlyHandler = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return steamInputTank.getCapacity(); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return steamInputTank.isFluidValid(stack); }
        @Override public int fill(FluidStack resource, FluidAction action) {
            int filled = steamInputTank.fill(resource, action);
            if (filled > 0 && action.execute()) setChanged();
            return filled;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    };

    /**
     * Drain-only handler exposed on all non-back faces (processed steam exits here).
     * Pipes cannot fill through this side, eliminating ambiguity.
     */
    private final IFluidHandler outputOnlyHandler = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return steamOutputTank.getFluid(); }
        @Override public int getTankCapacity(int tank) { return steamOutputTank.getCapacity(); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack drained = steamOutputTank.drain(resource, action);
            if (!drained.isEmpty() && action.execute()) setChanged();
            return drained;
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack drained = steamOutputTank.drain(maxDrain, action);
            if (!drained.isEmpty() && action.execute()) setChanged();
            return drained;
        }
    };

    public int steamInputAmount() {
        return steamInputTank.getFluidAmount();
    }
    public int steamInputCapacity() {
        return steamInputTank.getCapacity();
    }
    public int steamOutputAmount() {
        return steamOutputTank.getFluidAmount();
    }
    public int steamOutputCapacity() {
        return steamOutputTank.getCapacity();
    }

    // Steam processed each tick over the last FLOW_WINDOW ticks (ring buffer) and its running sum
    private final int[] flowHistory = new int[FLOW_WINDOW];
    private int flowIndex = 0;
    private int flowSum = 0;
    // Average steam processed per tick (mB) — determines stress capacity
    private float steamFlow = 0f;
    private boolean active = false;
    private float lastCapacity = 0f;

    public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(CNIBlocks.STEAM_TURBINE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    // ── Kinetic output ────────────────────────────────────────────────────────

    @Override
    public float getGeneratedSpeed() {
        return active ? SPEED : 0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!active) return 0f;
        // Create multiplies this by RPM, so divide by SPEED to land on MAX_SU at full flow
        return (steamFlow / MAX_THROUGHPUT) * Config.MAX_SU.get() / SPEED;
    }

    // ── Per-tick processing ───────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;

        // Only take in as much steam as the exhaust has room to pass on, so a slow or missing
        // exhaust pipe backs the turbine up instead of silently destroying steam
        int exhaustRoom = TANK_CAPACITY - steamOutputTank.getFluidAmount();
        int fitsExhaust = (int) (exhaustRoom / (1f - CONSUMPTION_RATIO));
        if (fitsExhaust < 10) fitsExhaust = 0; // exhaust effectively blocked: stop, don't nibble
        int available = Math.min(Math.min(MAX_THROUGHPUT, fitsExhaust), steamInputTank.getFluidAmount());
        int processed = 0;

        if (available > 0) {
            int passThrough = (int) (available * (1f - CONSUMPTION_RATIO));
            processed = available;

            steamInputTank.drain(available, IFluidHandler.FluidAction.EXECUTE);
            if (passThrough > 0)
                steamOutputTank.fill(
                        new FluidStack(CNIFluids.STEAM_STILL.get(), passThrough),
                        IFluidHandler.FluidAction.EXECUTE);
            setChanged();
        }

        flowSum += processed - flowHistory[flowIndex];
        flowHistory[flowIndex] = processed;
        flowIndex = (flowIndex + 1) % FLOW_WINDOW;
        steamFlow = (float) flowSum / FLOW_WINDOW;
        boolean isActive = steamFlow >= MIN_FLOW;

        if (isActive != active) {
            active = isActive;
            lastCapacity = calculateAddedStressCapacity();
            // Speed crossing zero — updateGeneratedRotation handles the full network
            // rebuild. Calling notifyStressCapacityChange on the same tick would
            // reach connected block entities (e.g. stress gauge) before their network
            // keys are restored, causing a NPE inside Create's KineticNetwork.
            updateGeneratedRotation();
        } else if (active && hasNetwork()) {
            // Speed unchanged, network is stable — just update the capacity value.
            float capacity = calculateAddedStressCapacity();
            if (Math.abs(capacity - lastCapacity) >= CAPACITY_EPSILON) {
                lastCapacity = capacity;
                notifyStressCapacityChange(capacity);
            }
        }
    }

    // ── Fluid API ─────────────────────────────────────────────────────────────

    public IFluidHandler getInputHandler()  { return inputOnlyHandler; }
    public IFluidHandler getOutputHandler() { return outputOnlyHandler; }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("steamIn",  steamInputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("steamOut", steamOutputTank.writeToNBT(registries, new CompoundTag()));
        // Saved so a reloaded turbine keeps running at the speed the network was saved with.
        // Without this it loads reporting 0 RPM, Create tears the network down and rebuilds
        // it a moment later, and that rebuild can break blocks (e.g. around a speed controller).
        tag.putIntArray("flowHistory", flowHistory);
        tag.putInt("flowIndex", flowIndex);
        tag.putBoolean("active", active);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("steamIn"))  steamInputTank.readFromNBT(registries,  tag.getCompound("steamIn"));
        if (tag.contains("steamOut")) steamOutputTank.readFromNBT(registries, tag.getCompound("steamOut"));
        if (!clientPacket && tag.contains("flowHistory")) {
            int[] saved = tag.getIntArray("flowHistory");
            if (saved.length == FLOW_WINDOW) {
                System.arraycopy(saved, 0, flowHistory, 0, FLOW_WINDOW);
                flowIndex = Math.floorMod(tag.getInt("flowIndex"), FLOW_WINDOW);
                flowSum = 0;
                for (int v : flowHistory) flowSum += v;
                steamFlow = (float) flowSum / FLOW_WINDOW;
                active = tag.getBoolean("active");
                lastCapacity = calculateAddedStressCapacity();
            }
        } else if (!clientPacket && getSpeed() != 0 && !hasSource() && !active) {
            // Saved by an older version without the flow history, while generating (spinning with
            // no other source driving it): assume it still is, with a token flow the real steam
            // replaces within the window
            java.util.Arrays.fill(flowHistory, 1);
            flowSum = FLOW_WINDOW;
            steamFlow = 1f;
            active = true;
            lastCapacity = calculateAddedStressCapacity();
        }
    }
}
