package com.createnuclearindustrys.Advencement.thermal_generator_energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;

import java.util.Optional;

public record ThermalGeneratorEnergyTriggerInstance(Optional<ContextAwarePredicate> player, float needEnergy)
        implements SimpleCriterionTrigger.SimpleInstance {
    public boolean matches(float energy) {
        return needEnergy <= energy;
    }
    public static final Codec<ThermalGeneratorEnergyTriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ThermalGeneratorEnergyTriggerInstance::player),
            Codec.FLOAT.fieldOf("energy").forGetter(ThermalGeneratorEnergyTriggerInstance::needEnergy)
    ).apply(inst, ThermalGeneratorEnergyTriggerInstance::new));
}