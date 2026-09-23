package com.createnuclearindustrys;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DirectionalBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Mod-bus event subscriber for common (server + client) setup that
 * doesn't belong in the main mod class — capability registrations live here.
 */
@EventBusSubscriber(modid = CreateNuclearIndustrys.MODID)
public class CommonModEvents {

    /**
     * Register the Steam Turbine's and Boiler's fluid handler capabilities so Create pipes
     * (and any other mod using Capabilities.FluidHandler.BLOCK) can pump water in
     * and steam out automatically.
     */
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CNIBlocks.STEAM_TURBINE_BLOCK_ENTITY.get(),
                (be, side) -> {
                    if (side == null) return be.getOutputHandler();
                    Direction facing = be.getBlockState().getValue(DirectionalBlock.FACING);
                    // Back face = steam inlet (fill-only)
                    if (side == facing.getOpposite()) return be.getInputHandler();
                    // All other faces = steam outlet (drain-only)
                    return be.getOutputHandler();
                }
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CNIBlocks.BOILER_BLOCK_ENTITY.get(),
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
