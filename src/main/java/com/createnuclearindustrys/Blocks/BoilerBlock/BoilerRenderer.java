package com.createnuclearindustrys.Blocks.BoilerBlock;

import com.createnuclearindustrys.Blocks.HeatSourceBlock.CreativeHeatSourceBlockEntity;
import com.createnuclearindustrys.CNIFluids;
import com.createnuclearindustrys.CreateNuclearIndustrys;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;

import static org.joml.Math.lerp;

public class BoilerRenderer implements BlockEntityRenderer<BoilerBlockEntity> {
    public BoilerRenderer(BlockEntityRendererProvider.Context context) {

    }
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
    @Override
    public void render(BoilerBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) return;

        int light = getLight(level, be.getBlockPos());

        poseStack.pushPose();

        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
                Fluids.WATER.defaultFluidState(),
                0.1f, 0.1f, 0.1f,  // x1, y1, z1
                0.9f, lerp(0.1f, 0.4f,(float) be.waterAmount() / be.waterCapacity()), 0.9f,  // x2, y2, z2
                buffer,
                poseStack,
                light,
                false,
                true
        );
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
                CNIFluids.STEAM_STILL.get().defaultFluidState(),
                0.1f, lerp(0.9f, 0.5f,(float) be.steamAmount() / be.steamCapacity()), 0.1f,  // x1, y1, z1
                0.9f, 0.9f, 0.9f,  // x2, y2, z2
                buffer,
                poseStack,
                light,
                false,
                true
        );

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