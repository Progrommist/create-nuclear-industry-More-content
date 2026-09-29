package com.createnuclearindustrys;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CreateNuclearIndustrys.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateNuclearIndustrys.MODID, value = Dist.CLIENT)
public class CreateNuclearIndustrysClient {
    public CreateNuclearIndustrysClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CNIPonderPlugin());

        CreateNuclearIndustrys.LOGGER.info("HELLO FROM CLIENT SETUP");
        CreateNuclearIndustrys.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
