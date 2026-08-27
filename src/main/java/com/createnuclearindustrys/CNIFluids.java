package com.createnuclearindustrys;

import com.createnuclearindustrys.Fluid.SteamFluid.SteamFluid;
import com.createnuclearindustrys.Fluid.SteamFluid.SteamFluidType;
import com.createnuclearindustrys.Fluid.UraniumFluid.UraniumFluid;
import com.createnuclearindustrys.Fluid.UraniumFluid.UraniumFluidType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class CNIFluids {
    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, CreateNuclearIndustrys.MODID);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, CreateNuclearIndustrys.MODID);
    private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, CreateNuclearIndustrys.MODID);

    // ── Fluid Types ─────────────────────────────────────────────────────────

    public static final DeferredHolder<FluidType, SteamFluidType> STEAM_FLUID_TYPE =
            FLUID_TYPES.register("steam", () -> new SteamFluidType(
                    FluidType.Properties.create()
                            .density(-200)
                            .viscosity(200)
                            .temperature(400)));

    public static final DeferredHolder<FluidType, UraniumFluidType> URANIUM_FLUID_TYPE =
            FLUID_TYPES.register("gaseous_uranium", () -> new UraniumFluidType(
                    FluidType.Properties.create()
                            .density(-200)
                            .viscosity(200)
                            .temperature(400)));

    // ── Fluids ──────────────────────────────────────────────────────────────

    public static final DeferredHolder<Fluid, SteamFluid.Still> STEAM_STILL =
            FLUIDS.register("steam", SteamFluid.Still::new);

    public static final DeferredHolder<Fluid, SteamFluid.Flowing> STEAM_FLOWING =
            FLUIDS.register("flowing_steam", SteamFluid.Flowing::new);

    public static final DeferredHolder<Fluid, UraniumFluid.Still> URANIUM_FLUID_STILL =
            FLUIDS.register("gaseous_uranium", UraniumFluid.Still::new);
    public static final DeferredHolder<Fluid, UraniumFluid.Flowing> URANIUM_FLUID_FLOWING =
            FLUIDS.register("flowing_gaseous_uranium", UraniumFluid.Flowing::new);

    // ── Particles ───────────────────────────────────────────────────────────

    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> STEAM_PARTICLE =
            PARTICLE_TYPES.register("steam", () -> new SimpleParticleType(false));

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
    }
}