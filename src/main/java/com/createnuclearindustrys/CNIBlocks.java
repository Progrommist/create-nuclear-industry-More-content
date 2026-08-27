package com.createnuclearindustrys;

import com.createnuclearindustrys.Blocks.BoronControlRod.BoronControlRod;
import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeBlock;
import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeBlockEntity;
import com.createnuclearindustrys.Blocks.HeatPipeBlock.HeatPipeBlock;
import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRodEntity;
import com.createnuclearindustrys.Fluid.SteamFluid.SteamFluidBlock;
import com.createnuclearindustrys.Blocks.ThermalGeneratorBlock.ThermalGeneratorBlock;
import com.createnuclearindustrys.Blocks.ThermalGeneratorBlock.ThermalGeneratorBlockEntity;
import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRod;
import com.createnuclearindustrys.Blocks.UraniumOreBlock.UraniumOreBlock;
import com.createnuclearindustrys.Blocks.ZincRodBlock.ZincRod;
import com.createnuclearindustrys.Fluid.UraniumFluid.UraniumFluidBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CNIBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateNuclearIndustrys.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateNuclearIndustrys.MODID);

    // ── Blocks ──────────────────────────────────────────────────────────────

    public static final DeferredBlock<HeatGaugeBlock> HEAT_GAUGE = BLOCKS.registerBlock("heat_gauge",
            HeatGaugeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                    .strength(2.0f, 6.0f).requiresCorrectToolForDrops());

    public static final DeferredBlock<BoronControlRod> BORON_CONTROL_ROD = BLOCKS.registerBlock("boron_control_rod",
            BoronControlRod::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE)
                    .strength(2.0f, 6.0f).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<UraniumFuelRod> URANIUM_FUEL_ROD = BLOCKS.registerBlock("uranium_fuel_rod",
            UraniumFuelRod::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN)
                    .strength(1.5f, 6.0f).requiresCorrectToolForDrops().noOcclusion()
                    .lightLevel(state -> state.getValue(UraniumFuelRod.HEAT_LEVEL)));

    public static final DeferredBlock<ZincRod> ZINC_ROD = BLOCKS.registerBlock("zinc_rod",
            ZincRod::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                    .strength(1.5f, 6.0f).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<HeatPipeBlock> HEAT_PIPE = BLOCKS.registerBlock("heat_pipe",
            HeatPipeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE)
                    .strength(1.5f, 6.0f).requiresCorrectToolForDrops());

    public static final DeferredBlock<UraniumOreBlock> URANIUM_ORE_BLOCK = BLOCKS.registerBlock("uranium_ore_block",
            UraniumOreBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f).requiresCorrectToolForDrops().randomTicks());

    public static final DeferredBlock<ThermalGeneratorBlock> THERMAL_GENERATOR = BLOCKS.registerBlock("thermal_generator",
            ThermalGeneratorBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                    .strength(3.0f, 8.0f).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<LiquidBlock> STEAM_BLOCK = BLOCKS.registerBlock("steam_fluid",
            p -> new SteamFluidBlock(CNIFluids.STEAM_STILL.get(), p),
            BlockBehaviour.Properties.of().noCollission().strength(100f).noLootTable().replaceable());

    public static final DeferredBlock<LiquidBlock> URANIUM_FLUID_BLOCK = BLOCKS.registerBlock("gaseous_uranium_fluid",
            p -> new UraniumFluidBlock(CNIFluids.URANIUM_FLUID_STILL.get(), p),
            BlockBehaviour.Properties.of().noCollission().strength(100f).noLootTable().replaceable());

    // ── Block Entities ──────────────────────────────────────────────────────

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatGaugeBlockEntity>> HEAT_GAUGE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("heat_gauge", () -> BlockEntityType.Builder.of(
                    HeatGaugeBlockEntity::new, HEAT_GAUGE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThermalGeneratorBlockEntity>> THERMAL_GENERATOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("thermal_generator", () -> BlockEntityType.Builder.of(
                    ThermalGeneratorBlockEntity::new, THERMAL_GENERATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UraniumFuelRodEntity>> URANIUM_FUEL_ROD_ENTITY =
            BLOCK_ENTITY_TYPES.register("uranium_fuel_rod", () -> BlockEntityType.Builder.of(
                    UraniumFuelRodEntity::new, URANIUM_FUEL_ROD.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}