package com.createnuclearindustrys;

import com.createnuclearindustrys.Fluid.SteamFluid.SteamParticle;
import com.createnuclearindustrys.Blocks.ThermalGeneratorBlock.ThermalGeneratorRenderer;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = CreateNuclearIndustrys.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerTooltip(CNIItems.BORON_CONTROL_ROD_ITEM.get());
            registerTooltip(CNIItems.URANIUM_FUEL_ROD_ITEM.get());
            registerTooltip(CNIItems.MUTATED_BREAD.get());
            registerTooltip(CNIItems.URANIUM_BREAD.get());
            registerTooltip(CNIItems.HEAT_GAUGE_ITEM.get());
            registerTooltip(CNIItems.HEAT_PIPE_ITEM.get());
            registerTooltip(CNIItems.THERMAL_GENERATOR_ITEM.get());
        });
    }

    private static void registerTooltip(Item item) {
        TooltipModifier.REGISTRY.register(item,
                new ItemDescription.Modifier(item, FontHelper.Palette.GRAY_AND_WHITE));
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CNIFluids.STEAM_PARTICLE.get(), SteamParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                CNIBlocks.THERMAL_GENERATOR_BLOCK_ENTITY.get(),
                ThermalGeneratorRenderer::new);
    }
}
