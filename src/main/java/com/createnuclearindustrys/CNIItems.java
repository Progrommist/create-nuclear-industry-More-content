package com.createnuclearindustrys;

import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRodItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CNIItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateNuclearIndustrys.MODID);

    // ── Simple Items ────────────────────────────────────────────────────────

    public static final DeferredItem<Item> URANIUM_ORE =
            ITEMS.registerSimpleItem("uranium_ore", new Item.Properties());
    public static final DeferredItem<Item> BORON_ORE =
            ITEMS.registerSimpleItem("boron_ore", new Item.Properties());
    public static final DeferredItem<Item> POWDERED_BORON =
            ITEMS.registerSimpleItem("powdered_boron", new Item.Properties());
    public static final DeferredItem<Item> BORON_CARBIDE =
            ITEMS.registerSimpleItem("boron_carbide", new Item.Properties());
    public static final DeferredItem<Item> BORON_PILLS =
            ITEMS.registerSimpleItem("boron_pills", new Item.Properties());
    public static final DeferredItem<Item> ENRICHED_URANIUM_POWDER =
            ITEMS.registerSimpleItem("enriched_uranium_powder", new Item.Properties());
    public static final DeferredItem<Item> URANIUM_PILLS =
            ITEMS.registerSimpleItem("uranium_pills", new Item.Properties());
    public static final DeferredItem<Item> YELLOW_CAKE_ITEM =
            ITEMS.registerSimpleItem("yellow_cake", new Item.Properties());
    public static final DeferredItem<Item> URANIUM_DOUGH =
            ITEMS.registerSimpleItem("uranium_dough", new Item.Properties());

    public static final DeferredItem<Item> POTASSIUM_IODIDE =
            ITEMS.registerSimpleItem("potassium_iodide",
                    new Item.Properties().food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationModifier(0.6f)
                            .effect(() -> new MobEffectInstance(CNIEffects.RADIATION_PROTECTION, 2400, 1), 1.0f)
                            .alwaysEdible()
                            .build()
                    )
            );
    public static final DeferredItem<Item> URANIUM_BREAD =
            ITEMS.registerSimpleItem("uranium_bread",
                    new Item.Properties().food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.5f)
                            .effect(() -> new MobEffectInstance(CNIEffects.RADIATION_SICKNESS, 100, 1), 0.5f)
                            .alwaysEdible()
                            .build()
                    )
            );
    public static final DeferredItem<Item> MUTATED_BREAD =
            ITEMS.registerSimpleItem("mutated_bread",
                    new Item.Properties().food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(1f)
                            .effect(() -> new MobEffectInstance(CNIEffects.RADIATION_SICKNESS, 200, 2), 0.5f)
                            .alwaysEdible()
                            .build()
                    )
            );


    public static final DeferredItem<Item> UNPROCESSED_URANIUM_FUEL_ROD =
            ITEMS.registerSimpleItem("unprocessed_uranium_fuel_rod", new Item.Properties());
    public static final DeferredItem<Item> UNPROCESSED_BORON_ROD =
            ITEMS.registerSimpleItem("unprocessed_boron_rod", new Item.Properties());

    // ── Block Items ─────────────────────────────────────────────────────────

    public static final DeferredItem<BlockItem> HEAT_GAUGE_ITEM =
            ITEMS.registerSimpleBlockItem("heat_gauge", CNIBlocks.HEAT_GAUGE);

    public static final DeferredItem<BlockItem> BORON_CONTROL_ROD_ITEM =
            ITEMS.registerSimpleBlockItem("boron_control_rod", CNIBlocks.BORON_CONTROL_ROD);

    public static final DeferredItem<BlockItem> URANIUM_FUEL_ROD_ITEM =
            ITEMS.register("uranium_fuel_rod",() -> new UraniumFuelRodItem(CNIBlocks.URANIUM_FUEL_ROD.get(),
                    new Item.Properties()
                            .durability(100000)
                            .stacksTo(1)
            )
    );

    public static final DeferredItem<BlockItem> ZINC_ROD_ITEM =
            ITEMS.registerSimpleBlockItem("zinc_rod", CNIBlocks.ZINC_ROD);

    public static final DeferredItem<BlockItem> HEAT_PIPE_ITEM =
            ITEMS.registerSimpleBlockItem("heat_pipe", CNIBlocks.HEAT_PIPE);

    public static final DeferredItem<BlockItem> URANIUM_ORE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("uranium_ore_block", CNIBlocks.URANIUM_ORE_BLOCK);

    public static final DeferredItem<BlockItem> THERMAL_GENERATOR_ITEM =
            ITEMS.registerSimpleBlockItem("thermal_generator", CNIBlocks.THERMAL_GENERATOR);


    // ── Bucket ──────────────────────────────────────────────────────────────

    public static final DeferredItem<BucketItem> STEAM_BUCKET =
            ITEMS.register("steam_bucket",
                    () -> new BucketItem(CNIFluids.STEAM_STILL.get(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<BucketItem> URANIUM_FLUID_BUCKET =
            ITEMS.register("gaseous_uranium_bucket",
                    () -> new BucketItem(CNIFluids.URANIUM_FLUID_STILL.get(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}