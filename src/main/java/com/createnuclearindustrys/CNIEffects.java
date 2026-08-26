package com.createnuclearindustrys;

import com.createnuclearindustrys.Effects.RadiationProtectionEffect;
import com.createnuclearindustrys.Effects.RadiationSicknessEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CNIEffects {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, CreateNuclearIndustrys.MODID);

    public static final DeferredHolder<MobEffect, RadiationSicknessEffect> RADIATION_SICKNESS =
            MOB_EFFECTS.register("radiation_sickness", RadiationSicknessEffect::new);
    public static final DeferredHolder<MobEffect, RadiationProtectionEffect> RADIATION_PROTECTION =
            MOB_EFFECTS.register("radiation_protection", RadiationProtectionEffect::new);

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}