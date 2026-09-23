package com.createnuclearindustrys.Blocks.HeatSourceBlock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class CreativeHeatSourceRenderer implements BlockEntityRenderer<CreativeHeatSourceBlockEntity> {

    private static final float DEGREES_PER_TICK = 3.0F;
    private static final long PERIOD = (long) (360.0F / DEGREES_PER_TICK); // тиков на полный оборот

    private final EntityRenderDispatcher entityRenderer;

    public CreativeHeatSourceRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(CreativeHeatSourceBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) return;

        Entity entity = be.getDisplayEntity(level);
        if (entity == null) return;

        entity.tickCount = (int) level.getGameTime();

        float time = level.getGameTime() + partialTick;

        int light = getLight(level, (BlockPos) be.getBlockPos());

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);

        float scale = 0.53125F;
        float maxSize = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (maxSize > 1.0F) {
            scale /= maxSize;
        }

        poseStack.translate(0.0F, 0.4F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * DEGREES_PER_TICK));
        poseStack.translate(0.0F, -0.2F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        poseStack.scale(scale, scale, scale);

        entityRenderer.setRenderShadow(false);
        entityRenderer.render(entity, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffer, light);
        entityRenderer.setRenderShadow(true);

        poseStack.popPose();
    }

    private static int getLight(Level level, BlockPos pos) {
        int block = level.getBrightness(LightLayer.BLOCK, pos);
        int sky = level.getBrightness(LightLayer.SKY, pos);
        for (Direction dir : Direction.values()) {
            BlockPos p = pos.relative(dir);
            block = Math.max(block, level.getBrightness(LightLayer.BLOCK, p));
            sky = Math.max(sky, level.getBrightness(LightLayer.SKY, p));
        }
        return LightTexture.pack(block, sky);
    }
}