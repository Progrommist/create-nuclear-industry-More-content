package com.createnuclearindustrys.Blocks.HeatSourceBlock;

import com.createnuclearindustrys.CNIBlocks;
import com.createnuclearindustrys.CreateNuclearIndustrys;
import com.createnuclearindustrys.Manament.RadiationManager;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;

public class CreativeHeatSourceBlockEntity extends SmartBlockEntity {

    // The board shows 0–200 steps; each step = 5°C (200 × 5 = 1000), major tick every 10 steps = 50°C
    private static final int STEP = 5;
    private static final int STEPS = 200;
    public ScrollValueBehaviour targetTemperature;

    public CreativeHeatSourceBlockEntity(BlockPos pos, BlockState state) {
        super(CNIBlocks.CREATIVE_HEAT_SOURCE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        targetTemperature = new ScrollValueBehaviour(
            Component.literal("Target Temperature"),
            this,
            new CenteredSideValueBoxTransform()
        ) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(
                    label, STEPS, 10,
                    List.of(Component.literal("Temperature")),
                    new ValueSettingsFormatter(vs -> Component.literal((vs.value() * STEP) + "°C"))
                );
            }

            @Override
            public void setValueSettings(Player player, ValueSettings settings, boolean onlyOnce) {
                setValue(settings.value() * STEP);
            }

            @Override
            public ValueSettings getValueSettings() {
                return new ValueSettings(0, value / STEP);
            }
        };
        targetTemperature.between(0, 1000);
        targetTemperature.value = 100;
        targetTemperature.withFormatter(v -> v + "°C");
        behaviours.add(targetTemperature);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;

        RadiationManager rm = RadiationManager.get((ServerLevel) level);
        rm.forceSetHeat(worldPosition, targetTemperature.value);
    }



    // This part made by AI
    // I don't know why he place it here
    // “If it works, don’t touch it. If it doesn’t work, touch everything.”
    private EntityType<?> entityType = EntityType.WITHER;
    @Nullable
    private Entity displayEntity;
    @Nullable
    public Entity getDisplayEntity(Level level) {
        if (displayEntity == null) {
            displayEntity = entityType.create(level);
        }
        return displayEntity;
    }
}
