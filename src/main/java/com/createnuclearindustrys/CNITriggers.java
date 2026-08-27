package com.createnuclearindustrys;

import com.createnuclearindustrys.Advencement.meltdown.MeltdownTrigger;
import com.createnuclearindustrys.Advencement.temperature.TemperatureTrigger;
import com.createnuclearindustrys.Advencement.thermal_generator_power.ThermalGeneratorTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CNITriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, CreateNuclearIndustrys.MODID);
    public static final Supplier<MeltdownTrigger> MELTDOWN_TRIGGER =
            TRIGGERS.register("meltdown_trigger", MeltdownTrigger::new);
    public static final Supplier<TemperatureTrigger> TEMPERATURE_TRIGGER =
            TRIGGERS.register("nuclear_temperature_trigger", TemperatureTrigger::new);
    public static final Supplier<ThermalGeneratorTrigger> THERMAL_GENERATOR_TRIGGER =
            TRIGGERS.register("thermal_generator_trigger", ThermalGeneratorTrigger::new);
}
