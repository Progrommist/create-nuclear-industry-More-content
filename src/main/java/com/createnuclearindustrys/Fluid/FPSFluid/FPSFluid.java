package com.createnuclearindustrys.Fluid.FPSFluid;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.CNIFluids;
import com.createnuclearindustrys.CNIItems;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class FPSFluid extends BaseFlowingFluid{
    static final Properties PROPERTIES = new Properties(
            () -> CNIFluids.FISSION_PRODUCT_SOLUTION_FLUID_TYPE.get(),
            () -> CNIFluids.FISSION_PRODUCT_SOLUTION_STILL.get(),
            () -> CNIFluids.FISSION_PRODUCT_SOLUTION_FLOWING.get())
            .block(() -> CNIBlocks.FISSION_PRODUCT_SOLUTION_BLOCK.get())
            .bucket(() -> CNIItems.FISSION_PRODUCT_SOLUTION_BUCKET.get())
            .slopeFindDistance(2);

    protected FPSFluid() {
        super(PROPERTIES);
    }

    // ── Still (source) ────────────────────────────────────────────────────────

    public static class Still extends FPSFluid {
        @Override public boolean isSource(FluidState state) { return true; }
        @Override public int getAmount(FluidState state)    { return 8;    }
    }

    // ── Flowing ───────────────────────────────────────────────────────────────

    public static class Flowing extends FPSFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
        @Override public boolean isSource(FluidState state) { return false;                    }
        @Override public int getAmount(FluidState state)    { return state.getValue(LEVEL);    }
    }
}
