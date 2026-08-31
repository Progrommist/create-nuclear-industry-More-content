package com.createnuclearindustrys.Advencement.thermal_generator_energy;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class ThermalGeneratorEnergyTrigger extends SimpleCriterionTrigger<ThermalGeneratorEnergyTriggerInstance> {
    @Override
    public Codec<ThermalGeneratorEnergyTriggerInstance> codec() { return ThermalGeneratorEnergyTriggerInstance.CODEC; }
    public void trigger(ServerPlayer player, float energy) {
        this.trigger(player, instance -> instance.matches(energy));
    }
}
