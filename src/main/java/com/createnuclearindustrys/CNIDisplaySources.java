package com.createnuclearindustrys;

import com.createnuclearindustrys.Blocks.BoilerBlock.BoilerSteamDisplaySource;
import com.createnuclearindustrys.Blocks.BoilerBlock.BoilerWaterDisplaySource;
import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeDisplaySource;
import com.createnuclearindustrys.Blocks.ReactimeterBlock.ReactimeterDisplaySource;
import com.createnuclearindustrys.Blocks.SteamTurbine.SteamTurbineSummarySteamDisplaySource;
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

    public static final DeferredHolder<DisplaySource, SteamTurbineSummarySteamDisplaySource> STEAM_TURBINE_SUMMARY_DISPLAY_SOURCE =
            DISPLAY_SOURCES.register("steam_turbine_summary", SteamTurbineSummarySteamDisplaySource::new);
    public static final DeferredHolder<DisplaySource, BoilerWaterDisplaySource> BOILER_WATER_DISPLAY_SOURCE =
            DISPLAY_SOURCES.register("boiler_water", BoilerWaterDisplaySource::new);
    public static final DeferredHolder<DisplaySource, BoilerSteamDisplaySource> BOILER_STEAM_DISPLAY_SOURCE =
            DISPLAY_SOURCES.register("boiler_steam", BoilerSteamDisplaySource::new);
    public static final DeferredHolder<DisplaySource, ReactimeterDisplaySource> REACTIMETER_DISPLAY_SOURCE =
            DISPLAY_SOURCES.register("reactimeter_reactivity", ReactimeterDisplaySource::new);

    public static void register(IEventBus modEventBus) {
        DISPLAY_SOURCES.register(modEventBus);
    }
}