package com.createnuclearindustrys;

import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeDisplaySource;
import com.createnuclearindustrys.Blocks.ReactimeterBlock.ReactimeterDisplaySource;
import com.createnuclearindustrys.Blocks.ThermalGeneratorBlock.ThermalGeneratorSteamDisplaySource;
import com.createnuclearindustrys.Blocks.ThermalGeneratorBlock.ThermalGeneratorWaterDisplaySource;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CNIDisplaySources {
    private static final DeferredRegister<DisplaySource> DISPLAY_SOURCES =
            DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, CreateNuclearIndustrys.MODID);

    public static final DeferredHolder<DisplaySource, HeatGaugeDisplaySource> HEAT_GAUGE_DISPLAY_SOURCE =
            DISPLAY_SOURCES.register("heat_gauge_temperature", HeatGaugeDisplaySource::new);

    public static final DeferredHolder<DisplaySource, ThermalGeneratorWaterDisplaySource> THERMAL_GENERATOR_WATER_DISPLAY_SOURCE =
            DISPLAY_SOURCES.register("thermal_generator_water", ThermalGeneratorWaterDisplaySource::new);

    public static final DeferredHolder<DisplaySource, ThermalGeneratorSteamDisplaySource> THERMAL_GENERATOR_STEAM_DISPLAY_SOURCE =
            DISPLAY_SOURCES.register("thermal_generator_steam", ThermalGeneratorSteamDisplaySource::new);
    public static final DeferredHolder<DisplaySource, ReactimeterDisplaySource> REACTIMETER_DISPLAY_SOURCE =
            DISPLAY_SOURCES.register("reactimeter_reactivity", ReactimeterDisplaySource::new);

    public static void register(IEventBus modEventBus) {
        DISPLAY_SOURCES.register(modEventBus);
    }
}