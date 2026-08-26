package com.createnuclearindustrys;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Mod-bus event subscriber for common (server + client) setup that
 * doesn't belong in the main mod class — capability registrations live here.
 */
@EventBusSubscriber(modid = CreateNuclearIndustrys.MODID)
public class CommonModEvents {

    /**
     * Register the Thermal Generator's fluid handler capability so Create pipes
     * (and any other mod using Capabilities.FluidHandler.BLOCK) can pump water in
     * and steam out automatically.
     */
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CNIBlocks.THERMAL_GENERATOR_BLOCK_ENTITY.get(),
                (be, side) -> be.getFluidHandler()
        );
    }
    @SubscribeEvent
    public static void PlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
        Inventory inv = event.getEntity().getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item.isEmpty()) continue;

            if (item.getItem() == CNIItems.URANIUM_BREAD.get()) {
                ItemStack newItemStack = new ItemStack(CNIItems.MUTATED_BREAD.get(), item.getCount());
                inv.setItem(i, newItemStack);
            }
        }
    }
}
