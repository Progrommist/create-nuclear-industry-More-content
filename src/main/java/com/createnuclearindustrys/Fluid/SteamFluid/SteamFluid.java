package com.createnuclearindustrys.Fluid.SteamFluid;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.CNIFluids;
import com.createnuclearindustrys.CNIItems;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/**
 * Steam fluid — source and flowing variants.
 * The fluid block (SteamFluidBlock) immediately dissipates on placement,
 * so this fluid is really only visible inside buckets and piping.
 */
public abstract class SteamFluid extends BaseFlowingFluid {

    /**
     * Circular-dependency-safe properties: all suppliers are lambdas that
     * capture static fields lazily, evaluated only when the fluid is
     * first instantiated during registration — never at class-load time.
     */
    static final BaseFlowingFluid.Properties PROPERTIES = new BaseFlowingFluid.Properties(
            () -> CNIFluids.STEAM_FLUID_TYPE.get(),
            () -> CNIFluids.STEAM_STILL.get(),
            () -> CNIFluids.STEAM_FLOWING.get())
            .block(() -> CNIBlocks.STEAM_BLOCK.get())
            .bucket(() -> CNIItems.STEAM_BUCKET.get())
            .levelDecreasePerBlock(2)   // doesn't flow far — it's steam
            .slopeFindDistance(2)
            .tickRate(4);               // dissipates quickly

    protected SteamFluid() {
        super(PROPERTIES);
    }

    // ── Still (source) ────────────────────────────────────────────────────────

    public static class Still extends SteamFluid {
        @Override public boolean isSource(FluidState state) { return true; }
        @Override public int getAmount(FluidState state)    { return 8;    }
    }

    // ── Flowing ───────────────────────────────────────────────────────────────

    public static class Flowing extends SteamFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
        @Override public boolean isSource(FluidState state) { return false;                    }
        @Override public int getAmount(FluidState state)    { return state.getValue(LEVEL);    }
    }
}
