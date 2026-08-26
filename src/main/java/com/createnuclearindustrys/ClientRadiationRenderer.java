package com.createnuclearindustrys;

import com.createnuclearindustrys.Blocks.BoronControlRod.BoronControlRod;
import com.createnuclearindustrys.Blocks.HeatPipeBlock.HeatPipeBlock;
import com.createnuclearindustrys.Manament.RadiationBirthPacket;
import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.*;

@EventBusSubscriber(modid = CreateNuclearIndustrys.MODID, value = Dist.CLIENT)
public class ClientRadiationRenderer {

    private static final Map<UUID, ClientParticle> particles = new HashMap<>();
    private static final RandomSource CLIENT_RNG = RandomSource.create();

    // ── Block color: tint shifts white → orange as heat_level increases ─────────

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            float t = state.getValue(UraniumFuelRod.HEAT_LEVEL) / 15f;
            int r = 255;
            int g = (int)(255 - t * 100); // 255 → 155
            int b = (int)(255 * (1f - t)); // 255 → 0
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        }, CNIBlocks.URANIUM_FUEL_ROD.get());
    }

    // ── Packet registration (mod bus, auto-detected via IModBusEvent) ──────────

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
            RadiationBirthPacket.TYPE,
            RadiationBirthPacket.STREAM_CODEC,
            (pkt, ctx) -> ctx.enqueueWork(() -> handleBirth(pkt))
        );
    }

    public static void handleBirth(RadiationBirthPacket pkt) {
        particles.put(pkt.id(), new ClientParticle(pkt));
    }

    // ── Client tick: step physics ─────────────────────────────────────────────

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) { particles.clear(); return; }

        List<UUID> dead = new ArrayList<>();
        for (ClientParticle p : particles.values()) {
            if (--p.ticksLeft <= 0) { dead.add(p.id); continue; }
            p.prevPos = p.pos;
            step(p, level);
        }
        dead.forEach(particles::remove);
    }

    // ── Render: draw colored lines ────────────────────────────────────────────

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (particles.isEmpty()) return;

        Camera camera  = event.getCamera();
        Vec3   camPos  = camera.getPosition();
        PoseStack pose = event.getPoseStack();
        PoseStack.Pose mat = pose.last();

        MultiBufferSource.BufferSource buf =
            Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lines = buf.getBuffer(RenderType.lines());

        for (ClientParticle p : particles.values()) {
            if (p.prevPos == null || p.prevPos.equals(p.pos)) continue;

            float x1 = (float)(p.prevPos.x - camPos.x);
            float y1 = (float)(p.prevPos.y - camPos.y);
            float z1 = (float)(p.prevPos.z - camPos.z);
            float x2 = (float)(p.pos.x - camPos.x);
            float y2 = (float)(p.pos.y - camPos.y);
            float z2 = (float)(p.pos.z - camPos.z);

            float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
            float len = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (len == 0) continue;
            float nx = dx / len, ny = dy / len, nz = dz / len;

            lines.addVertex(mat, x1, y1, z1).setColor(p.r, p.g, p.b, 1f).setNormal(mat, nx, ny, nz);
            lines.addVertex(mat, x2, y2, z2).setColor(p.r, p.g, p.b, 1f).setNormal(mat, nx, ny, nz);
        }

        buf.endBatch(RenderType.lines());
    }

    // ── Physics (mirrors server-side RadiationManager.step exactly) ──────────

    private static void step(ClientParticle p, ClientLevel level) {
        Vec3 next = p.pos.add(p.vel);
        if (!level.isLoaded(BlockPos.containing(next))) return;

        if (BlockPos.containing(p.pos).equals(p.source)) {
            p.pos = next;
            return;
        }

        if (!isPointInSolid(level, next)) {
            p.pos = next;
            return;
        }

        BlockPos hitBlock = BlockPos.containing(next);

        // Mirror server absorption so client particles fade at the same rate
        if (CLIENT_RNG.nextFloat() < getAbsorption(level.getBlockState(hitBlock))) {
            p.ticksLeft = 0;
            return;
        }

        double vx = p.vel.x, vy = p.vel.y, vz = p.vel.z;
        if (isPointInSolid(level, new Vec3(p.pos.x + p.vel.x, p.pos.y,            p.pos.z           ))) vx = -vx;
        if (isPointInSolid(level, new Vec3(p.pos.x,            p.pos.y + p.vel.y, p.pos.z           ))) vy = -vy;
        if (isPointInSolid(level, new Vec3(p.pos.x,            p.pos.y,            p.pos.z + p.vel.z))) vz = -vz;
        if (vx == p.vel.x && vy == p.vel.y && vz == p.vel.z) { vx = -vx; vy = -vy; vz = -vz; }
        p.vel = new Vec3(vx, vy, vz);
    }

    private static float getAbsorption(BlockState state) {
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

    static boolean isPointInSolid(ClientLevel level, Vec3 point) {
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

    // ── Client-side particle data ─────────────────────────────────────────────

    static class ClientParticle {
        final UUID id;
        Vec3 prevPos, pos, vel;
        final float r, g, b;
        final float energy;
        int ticksLeft;
        final BlockPos source;

        ClientParticle(RadiationBirthPacket pkt) {
            this.id        = pkt.id();
            this.pos       = new Vec3(pkt.px(), pkt.py(), pkt.pz());
            this.prevPos   = this.pos;
            this.vel       = new Vec3(pkt.vx(), pkt.vy(), pkt.vz());
            this.r         = pkt.r();
            this.g         = pkt.g();
            this.b         = pkt.b();
            this.energy    = pkt.energy();
            this.ticksLeft = pkt.lifetime();
            this.source    = BlockPos.of(pkt.sourcePos());
        }
    }
}
