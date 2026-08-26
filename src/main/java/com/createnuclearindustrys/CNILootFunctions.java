package com.createnuclearindustrys;

import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRodLootTable;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CNILootFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, CreateNuclearIndustrys.MODID);

    public static final Supplier<LootItemFunctionType<UraniumFuelRodLootTable>> URANIUM_FUEL_ROD_LOOT_TABLE =
            LOOT_FUNCTIONS.register(
                    "uranium_fuel_rod_loot",
                    () -> new LootItemFunctionType<>(UraniumFuelRodLootTable.CODEC)
            );
}
