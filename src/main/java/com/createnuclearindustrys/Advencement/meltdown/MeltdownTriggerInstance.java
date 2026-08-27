package com.createnuclearindustrys.Advencement.meltdown;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record MeltdownTriggerInstance(Optional<ContextAwarePredicate> player)
            implements SimpleCriterionTrigger.SimpleInstance {
        public boolean matches() {
            return true;
        }
        public static final Codec<MeltdownTriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(MeltdownTriggerInstance::player)
        ).apply(inst, MeltdownTriggerInstance::new));
}
