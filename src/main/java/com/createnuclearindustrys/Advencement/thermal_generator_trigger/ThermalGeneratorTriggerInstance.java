package com.createnuclearindustrys.Advencement.thermal_generator_trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;

import java.util.Optional;

public record ThermalGeneratorTriggerInstance(Optional<ContextAwarePredicate> player)
            implements SimpleCriterionTrigger.SimpleInstance {
        public boolean matches() {
            return true;
        }
        public static final Codec<ThermalGeneratorTriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ThermalGeneratorTriggerInstance::player)
        ).apply(inst, ThermalGeneratorTriggerInstance::new));
}
