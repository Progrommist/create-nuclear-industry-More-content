package com.createnuclearindustrys.Advencement.temperature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;

import java.util.Optional;

public record TemperatureTriggerInstance(Optional<ContextAwarePredicate> player, float min, float max)
            implements SimpleCriterionTrigger.SimpleInstance {
        public boolean matches(float temperature) {
            return temperature >= min && temperature <= max;
        }
        public static final Codec<TemperatureTriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TemperatureTriggerInstance::player),
                Codec.FLOAT.fieldOf("min").forGetter(TemperatureTriggerInstance::min),
                Codec.FLOAT.fieldOf("max").forGetter(TemperatureTriggerInstance::max)
        ).apply(inst, TemperatureTriggerInstance::new));
}
