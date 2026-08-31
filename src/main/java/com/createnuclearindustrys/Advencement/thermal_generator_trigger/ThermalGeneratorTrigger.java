package com.createnuclearindustrys.Advencement.thermal_generator_trigger;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class ThermalGeneratorTrigger extends SimpleCriterionTrigger<ThermalGeneratorTriggerInstance> {

    @Override
    public Codec<ThermalGeneratorTriggerInstance> codec() { return ThermalGeneratorTriggerInstance.CODEC; }
    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> instance.matches());
    }
}
