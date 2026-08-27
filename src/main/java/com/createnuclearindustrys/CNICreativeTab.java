package com.createnuclearindustrys;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class CNICreativeTab {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateNuclearIndustrys.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.createnuclearindustrys"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> CNIItems.URANIUM_FUEL_ROD_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(CNIItems.URANIUM_ORE_BLOCK_ITEM.get());
                        output.accept(CNIItems.URANIUM_ORE.get());
                        output.accept(CNIItems.YELLOW_CAKE_ITEM.get());
                        output.accept(CNIItems.URANIUM_FLUID_BUCKET.get());
                        output.accept(CNIItems.ENRICHED_URANIUM_POWDER.get());
                        output.accept(CNIItems.URANIUM_PILLS.get());
                        output.accept(CNIItems.URANIUM_DOUGH.get());
                        output.accept(CNIItems.URANIUM_FUEL_ROD_ITEM.get());
                        output.accept(CNIItems.URANIUM_BREAD.get());
                        output.accept(CNIItems.MUTATED_BREAD.get());

                        output.accept(CNIItems.BORON_ORE.get());
                        output.accept(CNIItems.POWDERED_BORON.get());
                        output.accept(CNIItems.BORON_CARBIDE.get());
                        output.accept(CNIItems.BORON_PILLS.get());
                        output.accept(CNIItems.BORON_CONTROL_ROD_ITEM.get());
                        output.accept(CNIItems.POTASSIUM_IODIDE.get());

                        output.accept(CNIItems.ZINC_ROD_ITEM.get());
                        output.accept(CNIItems.HEAT_GAUGE_ITEM.get());
                        output.accept(CNIItems.HEAT_PIPE_ITEM.get());
                        output.accept(CNIItems.THERMAL_GENERATOR_ITEM.get());
                        output.accept(CNIItems.STEAM_BUCKET.get());
                    }).build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}