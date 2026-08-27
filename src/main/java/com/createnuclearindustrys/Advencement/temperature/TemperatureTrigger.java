package com.createnuclearindustrys.Advencement.temperature;

import com.createnuclearindustrys.Advencement.meltdown.MeltdownTriggerInstance;
import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class TemperatureTrigger extends SimpleCriterionTrigger<TemperatureTriggerInstance> {

    @Override
    public Codec<TemperatureTriggerInstance> codec() { return TemperatureTriggerInstance.CODEC; }
    public void trigger(ServerPlayer player, float temperature) {
        this.trigger(player, instance -> instance.matches(temperature));
    }
}
