package com.createnuclearindustrys.Blocks.PlutoniumFuelRod;

import com.createnuclearindustrys.CNILootFunctions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class PlutoniumFuelRodLootTable extends LootItemConditionalFunction {

    protected PlutoniumFuelRodLootTable(List<LootItemCondition> predicates) {
        super(predicates);
    }
    public static final MapCodec<PlutoniumFuelRodLootTable> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance).apply(instance, PlutoniumFuelRodLootTable::new)
    );
    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return CNILootFunctions.PLUTONIUM_FUEL_ROD_LOOT_TABLE.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        BlockEntity blockEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);

        if (blockEntity instanceof PlutoniumFuelRodEntity fuelRodEntity) {
            int durability = fuelRodEntity.getDurability();

            if (stack.isDamageableItem()) {
                int maxDamage = stack.getMaxDamage();
                int damage = maxDamage - durability;
                stack.setDamageValue(Math.max(0, Math.min(damage, maxDamage)));
            }
        }

        return stack;
    }
}
