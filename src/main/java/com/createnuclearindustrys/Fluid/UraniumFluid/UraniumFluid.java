package com.createnuclearindustrys.Fluid.UraniumFluid;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.CNIFluids;
import com.createnuclearindustrys.CNIItems;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class UraniumFluid extends BaseFlowingFluid{
    static final BaseFlowingFluid.Properties PROPERTIES = new BaseFlowingFluid.Properties(
            () -> CNIFluids.URANIUM_FLUID_TYPE.get(),
            () -> CNIFluids.URANIUM_FLUID_STILL.get(),
            () -> CNIFluids.URANIUM_FLUID_FLOWING.get())
            .block(() -> CNIBlocks.URANIUM_FLUID_BLOCK.get())
            .bucket(() -> CNIItems.URANIUM_FLUID_BUCKET.get())
            .levelDecreasePerBlock(2)   // doesn't flow far — it's steam
            .slopeFindDistance(2)
            .tickRate(4);               // dissipates quickly

    protected UraniumFluid() {
        super(PROPERTIES);
    }

    // ── Still (source) ────────────────────────────────────────────────────────

    public static class Still extends UraniumFluid {
        @Override public boolean isSource(FluidState state) { return true; }
        @Override public int getAmount(FluidState state)    { return 8;    }
    }

    // ── Flowing ───────────────────────────────────────────────────────────────

    public static class Flowing extends UraniumFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
        @Override public boolean isSource(FluidState state) { return false;                    }
        @Override public int getAmount(FluidState state)    { return state.getValue(LEVEL);    }
    }
}
