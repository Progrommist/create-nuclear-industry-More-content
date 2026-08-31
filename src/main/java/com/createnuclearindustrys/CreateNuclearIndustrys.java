package com.createnuclearindustrys;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import java.util.List;

@Mod(CreateNuclearIndustrys.MODID)
public class CreateNuclearIndustrys {
    public static final String MODID = "createnuclearindustrys";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateNuclearIndustrys(IEventBus modEventBus, ModContainer modContainer) {

        CNIFluids.register(modEventBus);
        CNIEffects.register(modEventBus);
        CNIDisplaySources.register(modEventBus);
        CNIBlocks.register(modEventBus);
        CNIItems.register(modEventBus);
        CNICreativeTab.register(modEventBus);
        CNILootFunctions.LOOT_FUNCTIONS.register(modEventBus);
        CNITriggers.TRIGGERS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");

        event.enqueueWork(() -> {
            DisplaySource.BY_BLOCK_ENTITY.register(
                    CNIBlocks.HEAT_GAUGE_BLOCK_ENTITY.get(),
                    List.of(CNIDisplaySources.HEAT_GAUGE_DISPLAY_SOURCE.get())
            );
        });

        event.enqueueWork(() -> {
            DisplaySource.BY_BLOCK_ENTITY.register(
                    CNIBlocks.THERMAL_GENERATOR_BLOCK_ENTITY.get(),
                    List.of(
                            CNIDisplaySources.THERMAL_GENERATOR_WATER_DISPLAY_SOURCE.get(),
                            CNIDisplaySources.THERMAL_GENERATOR_STEAM_DISPLAY_SOURCE.get()
                    )
            );
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}