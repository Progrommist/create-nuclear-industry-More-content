package com.createnuclearindustrys.Advencement.meltdown;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class MeltdownTrigger extends SimpleCriterionTrigger<MeltdownTriggerInstance> {

    @Override
    public Codec<MeltdownTriggerInstance> codec() { return MeltdownTriggerInstance.CODEC; }
    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> instance.matches());
    }
}
