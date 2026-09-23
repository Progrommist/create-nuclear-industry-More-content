package com.createnuclearindustrys;

import com.createnuclearindustrys.Blocks.BoronControlRod.BoronControlRod;
import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Cherenkov radiation: the blue glow of a working reactor seen through water.
 *
 * Drawn as light rather than paint — soft camera-facing sprites blended additively
 * (the way enderman eyes glow), so they brighten whatever is behind them instead of
 * tinting it. A white-blue core fades through electric blue into a wide faint wash
 * that lights the water around the rod, and neighbouring rods add up.
 *
 * Only submerged rods that are actively emitting glow. The client already hears about
 * every radiation particle a rod emits, so that is how active rods are found — no
 * world scanning.
 *
 * Water is drawn before this stage and writes depth, so a depth-tested glow would vanish
 * under the pool surface. The glow is drawn with the depth test off instead, and a few rays
 * from the camera (passing through water, stopping at solid blocks) fade out rods hidden
 * behind walls.
 */
@EventBusSubscriber(modid = CreateNuclearIndustrys.MODID, value = Dist.CLIENT)
public class CherenkovGlowRenderer {

    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CreateNuclearIndustrys.MODID, "textures/misc/cherenkov_glow.png");

    private static final RenderType GLOW = RenderType.create(
            "createnuclearindustrys_cherenkov_glow",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_EYES_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(GLOW_TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    // A rod counts as active for this long after it last emitted a particle
    private static final long ACTIVE_TICKS = 40;
    private static final double MAX_DISTANCE = 64;
    // Glow at the coolest submerged rod vs. one at meltdown temperature
    private static final float MIN_INTENSITY = 0.25f;

    // Round layers, inside out: {radius in blocks, red, green, blue, strength}
    private static final float[][] LAYERS = {
            {0.70f, 0.75f, 0.90f, 1.00f, 0.90f}, // near-white core
            {1.70f, 0.20f, 0.50f, 1.00f, 0.75f}, // electric blue
            {4.00f, 0.05f, 0.20f, 1.00f, 0.55f}, // saturated wash that turns the pool blue
    };
    // Glow stretched along the rod, like light running up a fuel assembly: {half width, half height, r, g, b, strength}
    private static final float[] COLUMN = {0.85f, 1.60f, 0.35f, 0.65f, 1.00f, 0.60f};

    private static final Map<BlockPos, Long> lastEmission = new HashMap<>();
    private static final Map<BlockPos, Float> visibility = new HashMap<>();
    // Latest raycast result per rod; rays are re-cast every few frames, the fade runs every frame
    private static final Map<BlockPos, Float> visibleTarget = new HashMap<>();
    private static final int RAYCAST_EVERY_FRAMES = 4;
    private static int frame = 0;

    /** Called for every radiation particle the server tells the client about. */
    public static void onEmission(long sourcePos, long gameTime) {
        lastEmission.put(BlockPos.of(sourcePos), gameTime);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // After particles: water is already drawn, and unlike the block-layer stages this one
        // is given the real camera transform
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (lastEmission.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        long now = level.getGameTime();
        frame++;
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        Quaternionf facing = camera.rotation();
        float time = now + event.getPartialTick().getGameTimeDeltaPartialTick(false);

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer out = buffers.getBuffer(GLOW);
        PoseStack.Pose pose = event.getPoseStack().last();

        Iterator<Map.Entry<BlockPos, Long>> it = lastEmission.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Long> entry = it.next();
            BlockPos pos = entry.getKey();
            if (now - entry.getValue() > ACTIVE_TICKS) {
                it.remove();
                visibility.remove(pos);
                visibleTarget.remove(pos);
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof UraniumFuelRod) || !state.getValue(UraniumFuelRod.WATERLOGGED)) continue;

            Vec3 center = Vec3.atCenterOf(pos);
            if (center.distanceToSqr(cam) > MAX_DISTANCE * MAX_DISTANCE) continue;

            // Fade in and out smoothly as the rod comes into and out of view
            Float target = visibleTarget.get(pos);
            if (target == null || Math.floorMod(frame + pos.hashCode(), RAYCAST_EVERY_FRAMES) == 0) {
                target = visibleFraction(level, camera, cam, pos, center);
                visibleTarget.put(pos, target);
            }
            float seen = visibility.getOrDefault(pos, 0f);
            seen += (target - seen) * 0.25f;
            visibility.put(pos, seen);

            float heat = state.getValue(UraniumFuelRod.HEAT_LEVEL) / 15f;
            // A slow shimmer, offset per rod so a reactor doesn't pulse in unison
            float shimmer = 0.92f + 0.08f * (float) Math.sin(time * 0.15f + (pos.hashCode() & 255));
            float intensity = (MIN_INTENSITY + (1f - MIN_INTENSITY) * heat) * seen * shimmer;
            if (intensity < 0.01f) continue;

            Vector3f at = new Vector3f((float) (center.x - cam.x), (float) (center.y - cam.y), (float) (center.z - cam.z));
            for (float[] layer : LAYERS)
                sprite(out, pose, at, facing, layer[0],
                        layer[1] * layer[4] * intensity, layer[2] * layer[4] * intensity, layer[3] * layer[4] * intensity);
            column(out, pose, at, camera, intensity);
        }

        // NO_DEPTH_TEST only works if the depth test is already off; make sure it is
        RenderSystem.disableDepthTest();
        buffers.endBatch(GLOW);
    }

    /** A camera-facing square centred on {@code at}; the texture's soft falloff makes it round. */
    private static void sprite(VertexConsumer out, PoseStack.Pose pose, Vector3f at, Quaternionf facing,
                               float radius, float r, float g, float b) {
        Matrix4f matrix = pose.pose();
        float[][] corners = {{-1, -1, 0, 1}, {-1, 1, 0, 0}, {1, 1, 1, 0}, {1, -1, 1, 1}};
        for (float[] c : corners) {
            Vector3f v = new Vector3f(c[0] * radius, c[1] * radius, 0).rotate(facing).add(at);
            out.addVertex(matrix, v.x, v.y, v.z)
                    .setColor(r, g, b, 1f)
                    .setUv(c[2], c[3])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(pose, 0, 1, 0);
        }
    }

    /** An upright sprite that turns to face the camera but stays vertical, stretching the glow along the rod. */
    private static void column(VertexConsumer out, PoseStack.Pose pose, Vector3f at, Camera camera, float intensity) {
        Vector3f side = new Vector3f(camera.getLeftVector()).mul(1, 0, 1);
        if (side.lengthSquared() < 1e-4f) return; // looking straight down: the round layers cover it
        side.normalize(COLUMN[0]);
        float h = COLUMN[1], k = COLUMN[5] * intensity;
        Matrix4f matrix = pose.pose();
        float[][] corners = {{-1, -1, 0, 1}, {-1, 1, 0, 0}, {1, 1, 1, 0}, {1, -1, 1, 1}};
        for (float[] c : corners) {
            Vector3f v = new Vector3f(side).mul(c[0]).add(0, c[1] * h, 0).add(at);
            out.addVertex(matrix, v.x, v.y, v.z)
                    .setColor(COLUMN[2] * k, COLUMN[3] * k, COLUMN[4] * k, 1f)
                    .setUv(c[2], c[3])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(pose, 0, 1, 0);
        }
    }

    /**
     * How much of the rod the camera can see: rays to the rod's centre and a little around it
     * pass through water but stop at solid blocks. Rods and control rods are thin, so rays that
     * hit one still count as seeing the glow.
     */
    private static float visibleFraction(ClientLevel level, Camera camera, Vec3 cam, BlockPos rod, Vec3 center) {
        Vector3f up = camera.getUpVector(), left = camera.getLeftVector();
        float[][] offsets = {{0, 0}, {0.45f, 0}, {-0.45f, 0}, {0, 0.45f}, {0, -0.45f}};
        int visible = 0;
        for (float[] o : offsets) {
            Vec3 target = center.add(left.x * o[0] + up.x * o[1], left.y * o[0] + up.y * o[1], left.z * o[0] + up.z * o[1]);
            BlockHitResult hit = level.clip(new ClipContext(cam, target,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(rod)) {
                visible++;
                continue;
            }
            Block blocker = level.getBlockState(hit.getBlockPos()).getBlock();
            if (blocker instanceof UraniumFuelRod || blocker instanceof BoronControlRod) visible++;
        }
        return visible / (float) offsets.length;
    }
}
