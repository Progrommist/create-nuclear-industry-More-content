package com.createnuclearindustrys.Effects;

import com.createnuclearindustrys.CNIEffects;
import com.createnuclearindustrys.CreateNuclearIndustrys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Radiation Sickness — accumulates 1 second of duration per radiation particle absorbed.
 *
 * Passive (attribute modifiers, always active while the effect is present):
 *   • Slowness  — movement speed −15 %
 *   • Weakness  — attack damage −4 (same as vanilla Weakness I)
 *
 * Every 2 seconds (shouldApplyEffectTickThisTick returns true):
 *   • Nausea  8 s  (the nauseating screen wobble of ARS)
 *   • Mining Fatigue II  4 s  (too sick to work)
 *   • 25 % chance: 0.5 hearts magic damage  (radiation burns / cellular damage)
 */
public class RadiationSicknessEffect extends MobEffect {

    public RadiationSicknessEffect() {
        super(MobEffectCategory.HARMFUL, 0x39d353); // sickly radioactive green

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(CreateNuclearIndustrys.MODID, "effect.radiation_sickness.slowness"),
                -0.15,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(CreateNuclearIndustrys.MODID, "effect.radiation_sickness.weakness"),
                -4.0,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    /** Called every 40 ticks (2 s) while the effect is active. */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.hasEffect(CNIEffects.RADIATION_PROTECTION)) {
            entity.removeEffect(MobEffects.CONFUSION);
            entity.removeEffect(MobEffects.DIG_SLOWDOWN);
            return true;
        }

        // Screen wobble — the signature symptom of acute radiation sickness
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, amplifier - 1, false, false));
        // Too sick to mine or swing a tool properly
        entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, amplifier, false, false));
        // Radiation burns / cellular damage — occasional direct harm
        entity.hurt(entity.damageSources().magic(), amplifier);
        return true;
    }

    /** Trigger every 40 ticks (2 seconds). */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
