package com.createnuclearindustrys.Manament;

import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeBlock;
import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeBlockEntity;
import com.createnuclearindustrys.Blocks.HeatPipeBlock.HeatPipeBlock;
import com.createnuclearindustrys.Blocks.ThermalGeneratorBlock.ThermalGeneratorBlock;
import com.createnuclearindustrys.Blocks.ThermalGeneratorBlock.ThermalGeneratorBlockEntity;
import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRod;
import com.createnuclearindustrys.CNITriggers;
import com.createnuclearindustrys.CreateNuclearIndustrys;
import com.createnuclearindustrys.Utills.Managment.CommonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import static com.createnuclearindustrys.Utills.Managment.CommonInfo.*;

public class RadiationTasks {
    public static void auto_discover(ServerLevel level, Set<BlockPos> rods, RadiationManager _manager) {
        /** Auto-discover heat nodes adjacent to the known network that weren't registered */
        List<BlockPos> toRegister = new ArrayList<>();
        for (BlockPos pos : new ArrayList<>(rods)) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (rods.contains(neighbor.immutable())) continue;
                if (!level.isLoaded(neighbor)) continue;
                if (isHeatNode(level.getBlockState(neighbor).getBlock()))
                    toRegister.add(neighbor.immutable());
            }
        }
        for (BlockPos pos : toRegister) _manager.registerRod(pos, level);
    }
    public static void meltdown_check(ServerLevel level, Set<BlockPos> rods, Map<BlockPos, Float> rodHeat, RadiationManager _manager, float MELTDOWN_TEMP){
        /** Dissipate heat; uranium rods melt at 1000°C, everything else just caps */
        List<BlockPos> melted = new ArrayList<>();
        for (Map.Entry<BlockPos, Float> entry : rodHeat.entrySet()) {
            float heat = entry.getValue();
            if (heat >= MELTDOWN_TEMP) {
                if (level.getBlockState(entry.getKey()).getBlock() instanceof UraniumFuelRod)
                    melted.add(entry.getKey());
            }
        }
        for (BlockPos pos : melted) {
            _manager.triggerMeltdown(pos, level);
            rods.remove(pos);
            rodHeat.remove(pos);
        }
        if (!melted.isEmpty()) _manager.setDirty();
    }

    public static void rod_exist_check(ServerLevel level, Set<BlockPos> rods, RadiationManager _manager) {
        /** Validate registered nodes still exist (handles pistons, explosions, /fill, etc.) */
        List<BlockPos> gone = new ArrayList<>();
        for (BlockPos pos : rods) {
            if (level.isLoaded(pos) && !CommonInfo.isHeatNode(level.getBlockState(pos).getBlock()))
                gone.add(pos);
        }
        for (BlockPos pos : gone) _manager.removeRod(pos, level);
    }
    public static void heat_dissipation(Map<BlockPos, Float> rodHeat) {
        /** heat dissipation */
        for (Map.Entry<BlockPos, Float> entry : rodHeat.entrySet()) {
            entry.setValue(entry.getValue() * 0.999f);
        }
    }
    public static void thermal_generator_work(ServerLevel level, Map<BlockPos, Float> rodHeat) {
        // Thermal generators actively drain heat from the network (heat → rotation + steam).
        // If the generator has no water it can't convert heat, so it neither cools the network
        // nor produces any rotation — making water supply act as the critical control variable.
        //
        for (Map.Entry<BlockPos, Float> entry : rodHeat.entrySet()) {
            if (!(level.getBlockState(entry.getKey()).getBlock() instanceof ThermalGeneratorBlock)) continue;
            float heat = entry.getValue();
            if (heat <= 10f) continue;
            // Gate: only drain heat when the generator is actually converting water to steam
            if (!(level.getBlockEntity(entry.getKey()) instanceof ThermalGeneratorBlockEntity tbe)
                    || !tbe.hasWater() || tbe.fullSteam()) continue;
            entry.setValue(Math.max(0f, heat - heat * 0.005f));
        }
    }

    public static void uranium_rods_conduction(ServerLevel level, Set<BlockPos> rods, Map<BlockPos, Float> rodHeat) {
        // Vertical conduction between stacked uranium rods
        for (BlockPos pos : new ArrayList<>(rods)) {
            if (!(level.getBlockState(pos).getBlock() instanceof UraniumFuelRod)) continue;
            BlockPos above = pos.above();
            if (!rods.contains(above)) continue;
            float heatHere  = rodHeat.getOrDefault(pos, 0f);
            float heatAbove = rodHeat.getOrDefault(above, 0f);
            float transfer  = (heatHere - heatAbove) * 0.20f;
            if (Math.abs(transfer) < 0.01f) continue;
            rodHeat.put(pos, heatHere - transfer);
            rodHeat.put(above.immutable(), heatAbove + transfer);
        }
    }

    public static void heat_pipe_conduction(ServerLevel level, Set<BlockPos> rods, Map<BlockPos, Float> rodHeat) {
        // Heat pipe block conduction: each pipe block equalizes with all 6 neighbors
        Set<Long> pipeProcessed = new HashSet<>();
        for (BlockPos pos : new ArrayList<>(rods)) {
            if (!(level.getBlockState(pos).getBlock() instanceof HeatPipeBlock)) continue;
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (!rods.contains(neighbor)) continue;
                long la = pos.asLong(), lb = neighbor.asLong();
                long key = la < lb ? la * 31L + lb : lb * 31L + la;
                if (!pipeProcessed.add(key)) continue;
                float heatA = rodHeat.getOrDefault(pos, 0f);
                float heatB = rodHeat.getOrDefault(neighbor, 0f);
                float transfer = (heatA - heatB) * 0.25f;
                if (Math.abs(transfer) < 0.01f) continue;
                rodHeat.put(pos, heatA - transfer);
                rodHeat.put(neighbor.immutable(), heatB + transfer);
            }
        }
    }

    public static void heat_sync(ServerLevel level, Map<BlockPos, Float> rodHeat, float MAX_TEMP) {
        // Sync heat_level block state to clients (drives light + tint)
        for (Map.Entry<BlockPos, Float> entry : rodHeat.entrySet()) {
            BlockPos pos = entry.getKey();
            float heat = entry.getValue();
            BlockState current = level.getBlockState(pos);
            if (current.getBlock() instanceof UraniumFuelRod) {
                int newLevel = Math.min(15, (int)(heat / MAX_TEMP * 15));
                if (current.getValue(UraniumFuelRod.HEAT_LEVEL) != newLevel)
                    level.setBlock(pos, current.setValue(UraniumFuelRod.HEAT_LEVEL, newLevel), 2);
            } else if (current.getBlock() instanceof HeatGaugeBlock
                    && level.getBlockEntity(pos) instanceof HeatGaugeBlockEntity be) {
                be.setHeat(heat);
            } else if (current.getBlock() instanceof ThermalGeneratorBlock
                    && level.getBlockEntity(pos) instanceof ThermalGeneratorBlockEntity tbe) {
                tbe.setHeat(heat);
            }
        }
    }
    public static void advancement_trigger(ServerLevel level, Map<BlockPos, Float> rodHeat) {
        float totalSU = 0;
        for (Map.Entry<BlockPos, Float> i : rodHeat.entrySet()) {
            CNITriggers.TEMPERATURE_TRIGGER.get().trigger(CommonInfo.findClosestPlayer(i.getKey().getCenter(), level), i.getValue());

            if (level.getBlockEntity(i.getKey()) instanceof ThermalGeneratorBlockEntity tgbe) {
                totalSU += tgbe.calculateAddedStressCapacity() * tgbe.getGeneratedSpeed();
            }
        }
        for (ServerPlayer i : level.players()) {
            CNITriggers.THERMAL_GENERATOR_ENERGY_TRIGGER.get().trigger(i, totalSU);
        }
    }
}
