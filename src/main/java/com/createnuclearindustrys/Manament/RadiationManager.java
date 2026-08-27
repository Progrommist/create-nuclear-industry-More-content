package com.createnuclearindustrys.Manament;

import com.createnuclearindustrys.*;
import com.createnuclearindustrys.Blocks.BoronControlRod.BoronControlRod;
import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeBlock;
import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeBlockEntity;
import com.createnuclearindustrys.Blocks.HeatPipeBlock.HeatPipeBlock;
import com.createnuclearindustrys.Blocks.ThermalGeneratorBlock.ThermalGeneratorBlock;
import com.createnuclearindustrys.Blocks.ThermalGeneratorBlock.ThermalGeneratorBlockEntity;
import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRod;
import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRodEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.*;

public class RadiationManager extends SavedData {
    private static final String DATA_ID = CreateNuclearIndustrys.MODID + "_radiation";
    private static final int EMIT_INTERVAL = 10;
    private static final float MELTDOWN_TEMP = 1000f;

    private final Map<UUID, RadiationParticle> particles = new LinkedHashMap<>();
    private final Set<BlockPos> rods = new HashSet<>();
    private final Map<BlockPos, Float> rodHeat = new HashMap<>();
    private final RandomSource rng = RandomSource.create();
    private final List<RadiationParticle> pendingBroadcast = new ArrayList<>();

    private static final int updateTime = 80;
    private int ticksToUpdate = 0;

    public static RadiationManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(RadiationManager::new, RadiationManager::load),
            DATA_ID
        );
    }

    public void registerRod(BlockPos pos, ServerLevel level) {
        rods.add(pos.immutable());
        rodHeat.put(pos.immutable(), 0f);
        setDirty();
    }
    public void removeRod(BlockPos pos, ServerLevel level) {
        rods.remove(pos);
        rodHeat.remove(pos);
        setDirty();
    }

    public void addHeat(BlockPos pos, float amount) {
        if (!rods.contains(pos)) return;
        rodHeat.merge(pos.immutable(), amount, Float::sum);
        setDirty();
    }

    public float getHeat(BlockPos pos) {
        return rodHeat.getOrDefault(pos, 0f);
    }
    public void decDurability(BlockPos pos, ServerLevel level) {
        BlockEntity current = level.getBlockEntity(pos);
        if (current instanceof UraniumFuelRodEntity ufre) {
            ufre.decDurability();
        }
        setDirty();
    }
    public int getDurability(BlockPos pos, ServerLevel level) {
        BlockEntity current = level.getBlockEntity(pos);
        if (current instanceof UraniumFuelRodEntity ufre) {
            return ufre.getDurability();
        }
        return 0;
    }

    public List<RadiationParticle> drainPendingBroadcast() {
        if (pendingBroadcast.isEmpty()) return List.of();
        List<RadiationParticle> out = new ArrayList<>(pendingBroadcast);
        pendingBroadcast.clear();
        return out;
    }

    public Collection<RadiationParticle> getParticles() {
        return Collections.unmodifiableCollection(particles.values());
    }

    public void tick(ServerLevel level) {
        // Validate registered nodes still exist (handles pistons, explosions, /fill, etc.)
        List<BlockPos> gone = new ArrayList<>();
        for (BlockPos pos : rods) {
            if (level.isLoaded(pos) && !isHeatNode(level.getBlockState(pos).getBlock()))
                gone.add(pos);
        }
        for (BlockPos pos : gone) removeRod(pos, level);

        // Auto-discover heat nodes adjacent to the known network that weren't registered
        // (catches blocks placed by /fill, commands, pistons, or FallingBlockEntity landing)
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
        for (BlockPos pos : toRegister) registerRod(pos, level);

        // Dissipate heat; uranium rods melt at 1000°C, everything else just caps
        List<BlockPos> melted = new ArrayList<>();
        for (Map.Entry<BlockPos, Float> entry : rodHeat.entrySet()) {
            float heat = entry.getValue();
            if (heat >= MELTDOWN_TEMP) {
                if (level.getBlockState(entry.getKey()).getBlock() instanceof UraniumFuelRod)
                    melted.add(entry.getKey());
            } else if (heat > 0f) {
                entry.setValue(heat * 0.999f);
            }
        }
        for (BlockPos pos : melted) {
            triggerMeltdown(pos, level);
            rods.remove(pos);
            rodHeat.remove(pos);
        }
        if (!melted.isEmpty()) setDirty();

        // Thermal generators actively drain heat from the network (heat → rotation + steam).
        // If the generator has no water it can't convert heat, so it neither cools the network
        // nor produces any rotation — making water supply act as the critical control variable.
        for (Map.Entry<BlockPos, Float> entry : rodHeat.entrySet()) {
            if (!(level.getBlockState(entry.getKey()).getBlock() instanceof ThermalGeneratorBlock)) continue;
            float heat = entry.getValue();
            if (heat <= 10f) continue;
            // Gate: only drain heat when the generator is actually converting water to steam
            if (!(level.getBlockEntity(entry.getKey()) instanceof ThermalGeneratorBlockEntity tbe)
                    || !tbe.hasWater() || tbe.fullSteam()) continue;
            entry.setValue(Math.max(0f, heat - heat * 0.005f));
        }

        // Vertical conduction between stacked uranium rods
        for (BlockPos pos : new ArrayList<>(rods)) {
            BlockPos above = pos.above();
            if (!rods.contains(above)) continue;
            float heatHere  = rodHeat.getOrDefault(pos, 0f);
            float heatAbove = rodHeat.getOrDefault(above, 0f);
            float transfer  = (heatHere - heatAbove) * 0.20f;
            if (Math.abs(transfer) < 0.01f) continue;
            rodHeat.put(pos, heatHere - transfer);
            rodHeat.put(above.immutable(), heatAbove + transfer);
        }

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
                float transfer = (heatA - heatB) * 0.5f;
                if (Math.abs(transfer) < 0.01f) continue;
                rodHeat.put(pos, heatA - transfer);
                rodHeat.put(neighbor.immutable(), heatB + transfer);
            }
        }

        // Sync heat_level block state to clients (drives light + tint)
        for (Map.Entry<BlockPos, Float> entry : rodHeat.entrySet()) {
            BlockPos pos = entry.getKey();
            float heat = entry.getValue();
            BlockState current = level.getBlockState(pos);
            if (current.getBlock() instanceof UraniumFuelRod) {
                int newLevel = Math.min(15, (int)(heat / MELTDOWN_TEMP * 15));
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

        // Emit particles — rate scales with heat to simulate criticality
        for (BlockPos rod : new ArrayList<>(rods)) {
            if (!level.isLoaded(rod)) continue;
            if (!(level.getBlockState(rod).getBlock() instanceof UraniumFuelRod)) continue;
            float heatFrac = Math.min(1f, rodHeat.getOrDefault(rod, 0f) / MELTDOWN_TEMP);
            // interval shrinks from 10 (cold) down to 1 (at meltdown temp) — 10× more particles
            int interval = Math.max(1, (int)(EMIT_INTERVAL * (1f - heatFrac * 0.9f)));
            if (rng.nextInt(interval) != 0) continue;
            emitFromRod(rod);
            // Near criticality: burst a second particle
            if (heatFrac > 0.8f && rng.nextFloat() < (heatFrac - 0.8f) * 5f)
                emitFromRod(rod);
        }

        List<UUID> dead = new ArrayList<>();
        for (RadiationParticle p : particles.values()) {
            if (--p.ticksLeft <= 0) { dead.add(p.id); continue; }
            step(p, level);
        }
        if (!dead.isEmpty()) { dead.forEach(particles::remove); setDirty(); }

        ticksToUpdate++;
        if (ticksToUpdate >= updateTime) {
            for (Map.Entry<BlockPos, Float> i : rodHeat.entrySet()) {
                CNITriggers.TEMPERATURE_TRIGGER.get().trigger(findClosestPlayer(i.getKey().getCenter(), level), i.getValue());
            }

            ticksToUpdate = 0;
        }
    }

    private void triggerMeltdown(BlockPos epicenter, ServerLevel level) {
        // Power scales with ALL registered rods nearby — big reactors should make bigger booms
        int nearbyRods = 0;
        for (BlockPos rodPos : rods) {
            if (!rodPos.equals(epicenter) && rodPos.distSqr(epicenter) <= 100)
                nearbyRods++;
        }

        // Fling surrounding blocks outward before the explosion
        int flingRadius = 6;
        for (int dx = -flingRadius; dx <= flingRadius; dx++) {
            for (int dy = -flingRadius; dy <= flingRadius; dy++) {
                for (int dz = -flingRadius; dz <= flingRadius; dz++) {
                    if (dx*dx + dy*dy + dz*dz > flingRadius * flingRadius) continue;
                    if (rng.nextFloat() > 0.15f) continue;

                    BlockPos scanPos = epicenter.offset(dx, dy, dz);
                    if (!level.isLoaded(scanPos)) continue;

                    BlockState state = level.getBlockState(scanPos);
                    if (state.isAir()) continue;
                    if (!state.getFluidState().isEmpty()) continue;
                    if (state.getDestroySpeed(level, scanPos) < 0) continue;

                    BlockState launchState = rng.nextFloat() < 0.25f ? Blocks.LAVA.defaultBlockState() : state;
                    FallingBlockEntity flung = FallingBlockEntity.fall(level, scanPos, launchState);

                    Vec3 dir = new Vec3(dx + 0.001, Math.max(dy, 0) + 0.6, dz + 0.001).normalize();
                    double speed = 0.6 + rng.nextDouble() * 1.4;
                    flung.setDeltaMovement(dir.scale(speed));
                    flung.dropItem = false;
                }
            }
        }

        // Explosion power: base 7, +1.5 per nearby rod, capped at 20
        float power = Math.min(20f, 7f + nearbyRods * 1.5f);
        level.explode(null,
            epicenter.getX() + 0.5, epicenter.getY() + 0.5, epicenter.getZ() + 0.5,
            power, true, Level.ExplosionInteraction.TNT);

        // Chain reaction: dump heat into nearby rods so they melt next tick
        // This ensures a big reactor cascades outward rather than fizzling
        for (BlockPos rodPos : new ArrayList<>(rods)) {
            if (!rodPos.equals(epicenter) && rodPos.distSqr(epicenter) <= 25) // 5-block radius
                addHeat(rodPos, MELTDOWN_TEMP * 1.2f);
        }

        if (level.isClientSide()) return;
        ServerPlayer closestPlayer = findClosestPlayer(epicenter.getCenter(), level);
        CreateNuclearIndustrys.LOGGER.info("server");
        if (closestPlayer != null) {
            ServerPlayer serverPlayer = (ServerPlayer) closestPlayer;
            CNITriggers.MELTDOWN_TRIGGER.get().trigger(serverPlayer);
        }
    }

    private static boolean isHeatNode(Block b) {
        return b instanceof UraniumFuelRod || b instanceof HeatGaugeBlock
                || b instanceof HeatPipeBlock || b instanceof ThermalGeneratorBlock;
    }
    @Nullable
    public ServerPlayer findClosestPlayer(Vec3 position, ServerLevel server) {
        List<ServerPlayer> players = server.players();
        ServerPlayer closest = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (ServerPlayer player : players) {
            double distanceSq = player.position().distanceToSqr(position);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closest = player;
            }
        }
        return closest;
    }

    public void emitFromOre(BlockPos pos) {
        double theta = rng.nextDouble() * Math.PI * 2;
        double phi   = Math.acos(2.0 * rng.nextDouble() - 1.0);
        double speed = 0.05 + rng.nextDouble() * 0.12;
        double vx    = speed * Math.sin(phi) * Math.cos(theta);
        double vy    = speed * Math.cos(phi);
        double vz    = speed * Math.sin(phi) * Math.sin(theta);

        RadiationParticle p = new RadiationParticle(
            UUID.randomUUID(), Vec3.atCenterOf(pos),
            new Vec3(vx, vy, vz),
            0.3f + rng.nextFloat() * 0.4f, 0.7f + rng.nextFloat() * 0.3f, 0.1f,
            0.15f + rng.nextFloat() * 0.1f, 30 + rng.nextInt(40), pos
        );
        particles.put(p.id, p);
        pendingBroadcast.add(p);
        setDirty();
    }

    private void emitFromRod(BlockPos pos) {
        double theta = rng.nextDouble() * Math.PI * 2;
        double phi   = Math.acos(2.0 * rng.nextDouble() - 1.0);
        double speed = 0.1 + rng.nextDouble() * 0.3;
        double vx    = speed * Math.sin(phi) * Math.cos(theta);
        double vy    = speed * Math.cos(phi);
        double vz    = speed * Math.sin(phi) * Math.sin(theta);

        float r = rng.nextFloat(), g = rng.nextFloat(), b = rng.nextFloat();
        float energy = 1.0f;
        int lifetime = 80 + rng.nextInt(120);

        RadiationParticle p = new RadiationParticle(
            UUID.randomUUID(), Vec3.atCenterOf(pos),
            new Vec3(vx, vy, vz), r, g, b, energy, lifetime, pos
        );
        particles.put(p.id, p);
        pendingBroadcast.add(p);
        setDirty();
    }

    private void step(RadiationParticle p, ServerLevel level) {
        Vec3 next = p.pos.add(p.vel);
        if (!level.isLoaded(BlockPos.containing(next))) return;

        if (BlockPos.containing(p.pos).equals(p.source)) { p.pos = next; return; }

        // Check if the particle passes through a player — if so, give them radiation sickness
        AABB particleHitbox = new AABB(next.x - 0.4, next.y - 0.4, next.z - 0.4,
                                       next.x + 0.4, next.y + 0.4, next.z + 0.4);
        List<Player> players = level.getEntitiesOfClass(Player.class, particleHitbox);
        if (!players.isEmpty()) {
            applyRadiationSickness(players.get(0));
            p.ticksLeft = 0; // absorbed by the player's body
            return;
        }

        if (!isPointInSolid(level, next)) { p.pos = next; return; }

        BlockPos hitBlock = BlockPos.containing(next);

        if (rng.nextFloat() < getAbsorption(level.getBlockState(hitBlock))) {
            p.ticksLeft = 0;
            return;
        }

        if (rods.contains(hitBlock) && !hitBlock.equals(p.source)
                && !hitBlock.equals(p.source.above()) && !hitBlock.equals(p.source.below())
                && !(level.getBlockState(hitBlock).getBlock() instanceof HeatPipeBlock)) {
            addHeat(hitBlock, p.energy * 5f);
            decDurability(hitBlock, level);
        }

        double vx = p.vel.x, vy = p.vel.y, vz = p.vel.z;
        if (isPointInSolid(level, new Vec3(p.pos.x + p.vel.x, p.pos.y,            p.pos.z           ))) vx = -vx;
        if (isPointInSolid(level, new Vec3(p.pos.x,            p.pos.y + p.vel.y, p.pos.z           ))) vy = -vy;
        if (isPointInSolid(level, new Vec3(p.pos.x,            p.pos.y,            p.pos.z + p.vel.z))) vz = -vz;
        if (vx == p.vel.x && vy == p.vel.y && vz == p.vel.z) { vx = -vx; vy = -vy; vz = -vz; }
        p.vel = new Vec3(vx, vy, vz);
    }

    /** Extends (or starts) the Radiation Sickness effect on the given player by +1 second. */
    private static void applyRadiationSickness(LivingEntity entity) {
        var holder = CNIEffects.RADIATION_SICKNESS;
        MobEffectInstance existing = entity.getEffect(holder);
        int newDuration = (existing != null ? existing.getDuration() : 0) + 20;
        int newAmplifier = existing != null ? existing.getAmplifier() + Math.max(Math.round(entity.level().getRandom().nextFloat() - 0.49f), 0) : 1; // 1 % chance to get more amplifier
        entity.addEffect(new MobEffectInstance(holder, newDuration, newAmplifier, false, true, true));
    }

    static float getAbsorption(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof BoronControlRod) return 0.6f;
        if (b == Blocks.IRON_BLOCK || b instanceof UraniumFuelRod || b instanceof HeatPipeBlock) return 0f;
        if (b == Blocks.GOLD_BLOCK || b == Blocks.DIAMOND_BLOCK || b == Blocks.NETHERITE_BLOCK) return 0.05f;
        if (b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN) return 0.4f;
        if (b == Blocks.STONE || b == Blocks.COBBLESTONE || b == Blocks.DEEPSLATE
                || b == Blocks.STONE_BRICKS || b == Blocks.BRICKS || b == Blocks.SANDSTONE) return 0.15f;
        if (b == Blocks.SAND || b == Blocks.RED_SAND || b == Blocks.GRAVEL
                || b == Blocks.DIRT || b == Blocks.GRASS_BLOCK) return 0.25f;
        return 0.1f;
    }

    private static boolean isPointInSolid(ServerLevel level, Vec3 point) {
        BlockPos pos = BlockPos.containing(point);
        if (!level.isLoaded(pos)) return true;
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        if (shape.isEmpty()) return false;
        double lx = point.x - pos.getX();
        double ly = point.y - pos.getY();
        double lz = point.z - pos.getZ();
        for (AABB aabb : shape.toAabbs()) {
            if (lx >= aabb.minX && lx <= aabb.maxX &&
                ly >= aabb.minY && ly <= aabb.maxY &&
                lz >= aabb.minZ && lz <= aabb.maxZ) return true;
        }
        return false;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag particleList = new ListTag();
        for (RadiationParticle p : particles.values()) particleList.add(p.save());
        tag.put("particles", particleList);
        tag.putLongArray("rods", rods.stream().mapToLong(BlockPos::asLong).toArray());
        CompoundTag heatTag = new CompoundTag();
        rodHeat.forEach((pos, heat) -> heatTag.putFloat(String.valueOf(pos.asLong()), heat));
        tag.put("rodHeat", heatTag);
        return tag;
    }

    public static RadiationManager load(CompoundTag tag, HolderLookup.Provider registries) {
        RadiationManager m = new RadiationManager();
        ListTag list = tag.getList("particles", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            RadiationParticle p = RadiationParticle.load(list.getCompound(i));
            m.particles.put(p.id, p);
        }
        for (long l : tag.getLongArray("rods")) m.rods.add(BlockPos.of(l));
        CompoundTag heatTag = tag.getCompound("rodHeat");
        for (String key : heatTag.getAllKeys()) {
            m.rodHeat.put(BlockPos.of(Long.parseLong(key)), heatTag.getFloat(key));
        }
        return m;
    }
}
